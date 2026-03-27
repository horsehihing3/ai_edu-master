import sys
import json
import base64
import fitz  # PyMuPDF
from PIL import Image
import io
import os

# 벡터 드로잉 클러스터링 파라미터
_DRAW_PROXIMITY_PT = 15    # 이 거리(pt) 이내 드로잉은 같은 그림으로 묶음
_DRAW_MIN_AREA_PT  = 4000  # 클러스터 최소 면적(pt²) — 선·밑줄 등 잡요소 제거
_DRAW_MIN_DIM_PT   = 40    # 클러스터 최소 가로/세로(pt) — 얇은 테두리 제거
_DRAW_OVERLAP_THR  = 0.30  # 래스터 이미지와 이 비율 이상 겹치면 중복으로 간주


def _cluster_drawings(drawings):
    """
    page.get_drawings() 결과를 근접 기준으로 클러스터링.
    작은 요소(선·밑줄 등)를 필터링 후, 충분한 크기의 클러스터 bbox 목록 반환.
    """
    # 각 path의 rect 추출 (면적 100pt² 미만 제외)
    rects = []
    for d in drawings:
        r = d.get("rect")
        if r is None:
            continue
        w = r.x1 - r.x0
        h = r.y1 - r.y0
        if w * h < 100:
            continue
        rects.append(r)

    if not rects:
        return []

    # Union-Find 방식으로 근접 rect 클러스터링
    parent = list(range(len(rects)))

    def find(i):
        while parent[i] != i:
            parent[i] = parent[parent[i]]
            i = parent[i]
        return i

    def union(i, j):
        parent[find(i)] = find(j)

    for i in range(len(rects)):
        for j in range(i + 1, len(rects)):
            ri, rj = rects[i], rects[j]
            # 두 rect가 proximity 이내로 인접하면 같은 클러스터
            if (ri.x0 - _DRAW_PROXIMITY_PT <= rj.x1 and
                ri.x1 + _DRAW_PROXIMITY_PT >= rj.x0 and
                ri.y0 - _DRAW_PROXIMITY_PT <= rj.y1 and
                ri.y1 + _DRAW_PROXIMITY_PT >= rj.y0):
                union(i, j)

    # 클러스터별 union bbox 계산
    clusters = {}
    for i, r in enumerate(rects):
        key = find(i)
        if key not in clusters:
            clusters[key] = [r.x0, r.y0, r.x1, r.y1]
        else:
            c = clusters[key]
            c[0] = min(c[0], r.x0)
            c[1] = min(c[1], r.y0)
            c[2] = max(c[2], r.x1)
            c[3] = max(c[3], r.y1)

    # 크기 필터 적용
    result = []
    for x0, y0, x1, y1 in clusters.values():
        w = x1 - x0
        h = y1 - y0
        if w * h >= _DRAW_MIN_AREA_PT and w >= _DRAW_MIN_DIM_PT and h >= _DRAW_MIN_DIM_PT:
            result.append({"x0": x0, "y0": y0, "x1": x1, "y1": y1})

    return result


def _overlaps_raster(cluster, raster_bboxes):
    """클러스터가 기존 래스터 bbox와 _DRAW_OVERLAP_THR 이상 겹치면 True."""
    cx0, cy0, cx1, cy1 = cluster["x0"], cluster["y0"], cluster["x1"], cluster["y1"]
    c_area = (cx1 - cx0) * (cy1 - cy0)
    if c_area <= 0:
        return True
    for rb in raster_bboxes:
        ix0 = max(cx0, rb["x0"])
        iy0 = max(cy0, rb["y0"])
        ix1 = min(cx1, rb["x1"])
        iy1 = min(cy1, rb["y1"])
        if ix1 > ix0 and iy1 > iy0:
            inter = (ix1 - ix0) * (iy1 - iy0)
            if inter / c_area >= _DRAW_OVERLAP_THR:
                return True
    return False


def extract_images_from_pdf(pdf_base64: str) -> list:
    """
    PDF에서 이미지가 포함된 문제의 이미지를 추출.
    반환: [{"problemNo": "12", "imageBase64": "...", "page": 2}, ...]
    """
    results = []
    pdf_bytes = base64.b64decode(pdf_base64)

    doc = fitz.open(stream=pdf_bytes, filetype="pdf")

    for page_index in range(len(doc)):
        page = doc[page_index]
        page_number = page_index + 1

        # 래스터 이미지 목록
        image_list = page.get_images(full=True)

        # 벡터 드로잉 클러스터 (미리 계산해서 skip 여부 판단)
        vector_clusters = _cluster_drawings(page.get_drawings())

        # 처리할 것이 없으면 skip (기존과 동일한 효과)
        if not image_list and not vector_clusters:
            continue

        # 페이지 전체 크기
        page_rect = page.rect
        page_width = page_rect.width
        page_height = page_rect.height

        # 페이지를 고해상도로 렌더링 (좌표 매핑용)
        mat = fitz.Matrix(3.0, 3.0)  # 3배 확대 (고해상도)
        pix = page.get_pixmap(matrix=mat)
        page_img = Image.frombytes("RGB", [pix.width, pix.height], pix.samples)
        render_scale_x = pix.width / page_width
        render_scale_y = pix.height / page_height

        # ── 기존 래스터 이미지 처리 (변경 없음) ──────────────────────────
        raster_bboxes = []  # 벡터 중복 제거용으로만 수집
        for img_index, img_info in enumerate(image_list):
            xref = img_info[0]

            try:
                # 이미지 크기 확인 (소형 이미지 제외)
                base_image = doc.extract_image(xref)
                img_width = base_image["width"]
                img_height = base_image["height"]

                if img_width < 80 or img_height < 80:
                    continue

                # PDF 페이지에서 이미지의 실제 위치(bbox) 찾기
                bbox = None
                for item in page.get_image_rects(xref):
                    bbox = item
                    break

                if bbox is None:
                    continue

                raster_bboxes.append({
                    "x0": bbox.x0, "y0": bbox.y0,
                    "x1": bbox.x1, "y1": bbox.y1
                })

                # bbox 좌표로 페이지 렌더링 이미지에서 크롭
                # 여백 5% 추가
                margin_x = (bbox.x1 - bbox.x0) * 0.05
                margin_y = (bbox.y1 - bbox.y0) * 0.05

                crop_x0 = max(0, (bbox.x0 - margin_x) * render_scale_x)
                crop_y0 = max(0, (bbox.y0 - margin_y) * render_scale_y)
                crop_x1 = min(pix.width,  (bbox.x1 + margin_x) * render_scale_x)
                crop_y1 = min(pix.height, (bbox.y1 + margin_y) * render_scale_y)

                cropped = page_img.crop((crop_x0, crop_y0, crop_x1, crop_y1))

                # 이미지를 base64로 인코딩
                buffer = io.BytesIO()
                cropped.save(buffer, format="PNG")
                img_base64 = base64.b64encode(buffer.getvalue()).decode("utf-8")

                # 이미지 위치의 y 비율로 문제번호 추정 (나중에 Vision으로 매핑)
                y_ratio = bbox.y0 / page_height

                results.append({
                    "page": page_number,
                    "imgIndex": img_index + 1,
                    "xref": xref,
                    "bbox": {
                        "x0": round(bbox.x0, 2),
                        "y0": round(bbox.y0, 2),
                        "x1": round(bbox.x1, 2),
                        "y1": round(bbox.y1, 2)
                    },
                    "pageWidth": round(page_width, 2),
                    "pageHeight": round(page_height, 2),
                    "yRatio": round(y_ratio, 4),
                    "imgWidth": img_width,
                    "imgHeight": img_height,
                    "imageBase64": img_base64
                })

            except Exception as e:
                print(f"WARN: 페이지 {page_number} 이미지 {img_index+1} 처리 실패: {e}", file=sys.stderr)
                continue

        # ── 벡터 드로잉 처리 (추가) ──────────────────────────────────────
        for v_idx, cluster in enumerate(vector_clusters):
            try:
                # 래스터 이미지와 겹치면 이미 처리된 영역 → skip
                if _overlaps_raster(cluster, raster_bboxes):
                    continue

                x0, y0, x1, y1 = cluster["x0"], cluster["y0"], cluster["x1"], cluster["y1"]

                # 텍스트 밀도 체크 — 보기 텍스트 박스(테두리만 있는 박스) 제외
                # 클러스터 bbox 안에 텍스트가 30% 이상 차지하면 도형이 아닌 텍스트 박스로 판단 → skip
                clip_rect = fitz.Rect(x0, y0, x1, y1)
                text_blocks = page.get_text("blocks", clip=clip_rect)
                cluster_area = (x1 - x0) * (y1 - y0)
                text_area = sum(
                    (b[2] - b[0]) * (b[3] - b[1])
                    for b in text_blocks if len(b) > 6 and b[6] == 0  # type 0 = text block
                )
                if cluster_area > 0 and text_area / cluster_area > 0.30:
                    continue  # 텍스트 박스(보기 테두리)로 판단 → 이미지 캡처 생략

                # 5% 여백 추가
                margin_x = (x1 - x0) * 0.05
                margin_y = (y1 - y0) * 0.05

                crop_x0 = max(0, (x0 - margin_x) * render_scale_x)
                crop_y0 = max(0, (y0 - margin_y) * render_scale_y)
                crop_x1 = min(pix.width,  (x1 + margin_x) * render_scale_x)
                crop_y1 = min(pix.height, (y1 + margin_y) * render_scale_y)

                cropped = page_img.crop((crop_x0, crop_y0, crop_x1, crop_y1))

                buffer = io.BytesIO()
                cropped.save(buffer, format="PNG")
                img_base64 = base64.b64encode(buffer.getvalue()).decode("utf-8")

                y_ratio = y0 / page_height

                results.append({
                    "page": page_number,
                    "imgIndex": -(v_idx + 1),   # 음수로 벡터 출처 구분 (Java 측 처리 동일)
                    "xref": -1,
                    "bbox": {
                        "x0": round(x0, 2),
                        "y0": round(y0, 2),
                        "x1": round(x1, 2),
                        "y1": round(y1, 2)
                    },
                    "pageWidth": round(page_width, 2),
                    "pageHeight": round(page_height, 2),
                    "yRatio": round(y_ratio, 4),
                    "imgWidth": int(x1 - x0),
                    "imgHeight": int(y1 - y0),
                    "imageBase64": img_base64,
                    "isVector": True   # 디버깅용 플래그 (Java에서 무시)
                })

            except Exception as e:
                print(f"WARN: 페이지 {page_number} 벡터 클러스터 {v_idx+1} 처리 실패: {e}", file=sys.stderr)
                continue

    doc.close()
    return results


def main():
    if len(sys.argv) < 2:
        print(json.dumps({"error": "Usage: python extract_images.py <base64_file_path>"}))
        sys.exit(1)

    file_path = sys.argv[1]

    try:
        with open(file_path, 'r') as f:
            pdf_base64 = f.read().strip()
        results = extract_images_from_pdf(pdf_base64)
        print(json.dumps(results, ensure_ascii=False))
    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)


if __name__ == "__main__":
    main()
