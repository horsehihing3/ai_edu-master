-- KaTeX 렌더링 테스트용 문제 업데이트
UPDATE problems SET
  question_text = '$$\\frac{1}{2} + \\frac{1}{3}$$의 값은?',
  explanation   = '$$\\frac{1}{2} + \\frac{1}{3} = \\frac{3}{6} + \\frac{2}{6} = \\frac{5}{6}$$'
WHERE problem_id = 70;

UPDATE problems SET
  question_text = '$$\\sqrt{16} + \\sqrt{9}$$의 값을 구하시오.',
  explanation   = '$$\\sqrt{16} = 4,\\; \\sqrt{9} = 3$$이므로 합은 7'
WHERE problem_id = 71;

UPDATE problems SET
  question_text = '다음 중 $$x^2 - 5x + 6 = 0$$의 해는?',
  explanation   = '$$(x-2)(x-3)=0$$이므로 $$x=2$$ 또는 $$x=3$$'
WHERE problem_id = 72;
