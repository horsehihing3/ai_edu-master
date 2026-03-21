-- problem 70 선택지: $$\frac{1}{2} + \frac{1}{3}$$ = 5/6 → 정답 5번
INSERT INTO problem_options (problem_id, option_no, option_text) VALUES
(70, 1, '$$\\frac{1}{3}$$'),
(70, 2, '$$\\frac{1}{4}$$'),
(70, 3, '$$\\frac{2}{3}$$'),
(70, 4, '$$\\frac{3}{4}$$'),
(70, 5, '$$\\frac{5}{6}$$');

UPDATE problems SET answer = '5' WHERE problem_id = 70;

-- problem 72 선택지: x²-5x+6=0 → x=2 또는 3 → 정답 2번
INSERT INTO problem_options (problem_id, option_no, option_text) VALUES
(72, 1, '$$x=1$$ 또는 $$x=4$$'),
(72, 2, '$$x=2$$ 또는 $$x=3$$'),
(72, 3, '$$x=-2$$ 또는 $$x=-3$$'),
(72, 4, '$$x=1$$ 또는 $$x=6$$'),
(72, 5, '$$x=0$$ 또는 $$x=5$$');

UPDATE problems SET answer = '2' WHERE problem_id = 72;
