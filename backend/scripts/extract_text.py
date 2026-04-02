import sys
import json
import base64
import fitz  # PyMuPDF


def extract_text_from_pdf(pdf_base64: str) -> dict:
    """
    PDF에서 페이지별 원본 텍스트를 추출.
    반환: {"page1": "...", "page2": "...", ...}
    Claude API 없이 PyMuPDF로 직접 추출 — 크레딧 절약용.
    """
    pdf_bytes = base64.b64decode(pdf_base64)
    doc = fitz.open(stream=pdf_bytes, filetype="pdf")
    pages = {}
    for i in range(len(doc)):
        text = doc[i].get_text("text")
        pages[f"page{i + 1}"] = text.strip()
    doc.close()
    return pages


def main():
    if len(sys.argv) < 2:
        print(json.dumps({"error": "Usage: python extract_text.py <base64_file_path>"}))
        sys.exit(1)

    file_path = sys.argv[1]
    try:
        with open(file_path, "r") as f:
            pdf_base64 = f.read().strip()
        result = extract_text_from_pdf(pdf_base64)
        print(json.dumps(result, ensure_ascii=False))
    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)


if __name__ == "__main__":
    main()
