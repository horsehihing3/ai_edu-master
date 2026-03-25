import sys
import json
import base64
import fitz  # PyMuPDF
from PIL import Image
import io
import os

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

        # 페이지에서 이미지 목록 추출
        image_list = page.get_images(full=True)

        if not image_list:
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
