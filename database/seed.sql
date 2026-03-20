-- ============================================================
-- AI 교육 플랫폼 - 샘플 데이터
-- 비밀번호: Test@1234 (교사/학생), Admin@1234 (관리자)
-- ============================================================

USE edu_platform;

-- ============================================================
-- 기존 데이터 정리 (재실행 가능하도록)
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE attempt_feedbacks;
TRUNCATE TABLE assignment_feedbacks;
TRUNCATE TABLE video_views;
TRUNCATE TABLE video_requests;
TRUNCATE TABLE videos;
TRUNCATE TABLE problem_attempts;
TRUNCATE TABLE bookmarks;
TRUNCATE TABLE learning_reports;
TRUNCATE TABLE learning_sessions;
TRUNCATE TABLE assignment_targets;
TRUNCATE TABLE assignment_problems;
TRUNCATE TABLE assignments;
TRUNCATE TABLE problem_options;
TRUNCATE TABLE problems;
TRUNCATE TABLE students;
TRUNCATE TABLE teachers;
TRUNCATE TABLE subscriptions;
TRUNCATE TABLE payments;
TRUNCATE TABLE inquiry_replies;
TRUNCATE TABLE inquiries;
DELETE FROM users WHERE email != 'admin@edu-platform.com';
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 사용자 (교사 1, 학생 3) - admin은 schema.sql에 포함
-- password_hash: BCrypt('Test@1234')
-- ============================================================
INSERT INTO users (email, password_hash, name, role, school_id, is_active, is_email_verified) VALUES
('teacher@test.com',   '$2a$10$GyUDBicEN/AfqG6DRtMv0uHiyX7nvUwyLNFQfIFDiSxQSdYIAFaLG', '김선생', 'TEACHER',  1, 1, 1),
('student_a@test.com', '$2a$10$GyUDBicEN/AfqG6DRtMv0uHiyX7nvUwyLNFQfIFDiSxQSdYIAFaLG', '김민준',  'STUDENT',  1, 1, 1),
('student_b@test.com', '$2a$10$GyUDBicEN/AfqG6DRtMv0uHiyX7nvUwyLNFQfIFDiSxQSdYIAFaLG', '이서연',  'STUDENT',  1, 1, 1),
('student_c@test.com', '$2a$10$GyUDBicEN/AfqG6DRtMv0uHiyX7nvUwyLNFQfIFDiSxQSdYIAFaLG', '박지호',  'STUDENT',  1, 1, 1);

-- ============================================================
-- 교사 상세
-- ============================================================
INSERT INTO teachers (user_id, school_id, subject, bio) VALUES
((SELECT user_id FROM users WHERE email='teacher@test.com'), 1, '수학', '10년 경력 수학 전문 교사입니다.');

-- ============================================================
-- 학생 상세
-- ============================================================
INSERT INTO students (user_id, school_id, grade, student_level, diagnosis_at) VALUES
((SELECT user_id FROM users WHERE email='student_a@test.com'), 1, 'GRADE_2', 'A', '2026-03-08 10:00:00'),
((SELECT user_id FROM users WHERE email='student_b@test.com'), 1, 'GRADE_1', 'B', '2026-03-08 10:00:00'),
((SELECT user_id FROM users WHERE email='student_c@test.com'), 1, 'GRADE_3', 'C', '2026-03-08 10:00:00');

-- ============================================================
-- 추가 사용자 (대시보드 통계용) - 과제 배정 전에 생성 필요
-- ============================================================
INSERT INTO users (email, password_hash, name, role, school_id, is_active, is_email_verified) VALUES
('teacher2@test.com',  '$2a$10$GyUDBicEN/AfqG6DRtMv0uHiyX7nvUwyLNFQfIFDiSxQSdYIAFaLG', '이선생',  'TEACHER', 1, 1, 1),
('student_d@test.com', '$2a$10$GyUDBicEN/AfqG6DRtMv0uHiyX7nvUwyLNFQfIFDiSxQSdYIAFaLG', '최지수',  'STUDENT', 1, 1, 1),
('student_e@test.com', '$2a$10$GyUDBicEN/AfqG6DRtMv0uHiyX7nvUwyLNFQfIFDiSxQSdYIAFaLG', '정우진',  'STUDENT', 1, 1, 1),
('student_f@test.com', '$2a$10$GyUDBicEN/AfqG6DRtMv0uHiyX7nvUwyLNFQfIFDiSxQSdYIAFaLG', '한예린',  'STUDENT', 1, 1, 1),
('student_g@test.com', '$2a$10$GyUDBicEN/AfqG6DRtMv0uHiyX7nvUwyLNFQfIFDiSxQSdYIAFaLG', '오민석',  'STUDENT', 1, 1, 1),
('student_h@test.com', '$2a$10$GyUDBicEN/AfqG6DRtMv0uHiyX7nvUwyLNFQfIFDiSxQSdYIAFaLG', '임소희',  'STUDENT', 1, 1, 1);

INSERT INTO teachers (user_id, school_id, subject, bio) VALUES
((SELECT user_id FROM users WHERE email='teacher2@test.com'), 1, '영어', '영어 전문 교사입니다.');

INSERT INTO students (user_id, school_id, grade, student_level, diagnosis_at) VALUES
((SELECT user_id FROM users WHERE email='student_d@test.com'), 1, 'GRADE_1', 'A', '2026-03-10 10:00:00'),
((SELECT user_id FROM users WHERE email='student_e@test.com'), 1, 'GRADE_2', 'B', '2026-03-10 10:00:00'),
((SELECT user_id FROM users WHERE email='student_f@test.com'), 1, 'GRADE_3', 'A', '2026-03-10 10:00:00'),
((SELECT user_id FROM users WHERE email='student_g@test.com'), 1, 'GRADE_1', 'C', '2026-03-11 10:00:00'),
((SELECT user_id FROM users WHERE email='student_h@test.com'), 1, 'GRADE_2', 'B', '2026-03-11 10:00:00');

-- ============================================================
-- 코드 ID 변수 설정 (system_codes 참조)
-- ============================================================
SET @subj_math = (SELECT code_id FROM system_codes WHERE code_group='SUBJECT' AND code_value='MATH'    LIMIT 1);
SET @subj_eng  = (SELECT code_id FROM system_codes WHERE code_group='SUBJECT' AND code_value='ENGLISH' LIMIT 1);
SET @subj_kor  = (SELECT code_id FROM system_codes WHERE code_group='SUBJECT' AND code_value='KOREAN'  LIMIT 1);
SET @subj_sci  = (SELECT code_id FROM system_codes WHERE code_group='SUBJECT' AND code_value='SCIENCE' LIMIT 1);
SET @subj_soc  = (SELECT code_id FROM system_codes WHERE code_group='SUBJECT' AND code_value='SOCIAL'  LIMIT 1);

-- 수학 단원
SET @unit_math_eq2     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_ALGEBRA_EQ2'     LIMIT 1);
SET @unit_math_quad    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_FUNCTION_QUAD'   LIMIT 1);
SET @unit_math_trig    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_FUNCTION_TRIG'   LIMIT 1);
SET @unit_math_exp     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_FUNCTION_EXP'    LIMIT 1);
SET @unit_math_seq     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_ALGEBRA_SEQ'     LIMIT 1);
SET @unit_math_linear  = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_FUNCTION_LINEAR' LIMIT 1);
SET @unit_math_sim     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_ALGEBRA_SIM'     LIMIT 1);
SET @unit_math_area    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_GEOMETRY_AREA'   LIMIT 1);
SET @unit_math_ratio   = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_NUMBER_RATIO'    LIMIT 1);
SET @unit_math_expr    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_ALGEBRA_EXPR'    LIMIT 1);
SET @unit_math_int     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_NUMBER_INT'      LIMIT 1);
SET @unit_math_factor  = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_NUMBER_FACTOR'   LIMIT 1);
SET @unit_math_arith   = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_NUMBER_ARITH'    LIMIT 1);
SET @unit_math_divis   = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_NUMBER_DIVIS'    LIMIT 1);
SET @unit_math_shape   = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='MATH_GEOMETRY_BASIC'  LIMIT 1);

-- 영어 단원
SET @unit_eng_infer    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='ENG_READING_INFER'  LIMIT 1);
SET @unit_eng_struct   = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='ENG_WRITING_STRUCT' LIMIT 1);
SET @unit_eng_main     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='ENG_READING_MAIN'   LIMIT 1);
SET @unit_eng_blank    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='ENG_READING_BLANK'  LIMIT 1);
SET @unit_eng_rel      = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='ENG_GRAMMAR_REL'    LIMIT 1);
SET @unit_eng_topic    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='ENG_READING_TOPIC'  LIMIT 1);
SET @unit_eng_tense    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='ENG_GRAMMAR_TENSE'  LIMIT 1);
SET @unit_eng_syn      = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='ENG_VOCAB_SYN'      LIMIT 1);
SET @unit_eng_prep     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='ENG_GRAMMAR_PREP'   LIMIT 1);

-- 국어 단원
SET @unit_kor_infer    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_NONLIT_INFER'  LIMIT 1);
SET @unit_kor_debate   = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_SPEECH_DEBATE' LIMIT 1);
SET @unit_kor_novel    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_LIT_MOD_NOVEL' LIMIT 1);
SET @unit_kor_fact     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_NONLIT_FACT'   LIMIT 1);
SET @unit_kor_poem     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_LIT_MOD_POEM'  LIMIT 1);
SET @unit_kor_sent     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_GRAMMAR_SENT'  LIMIT 1);
SET @unit_kor_parts    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_GRAMMAR_PARTS' LIMIT 1);
SET @unit_kor_cls      = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_LIT_CLS_POEM'  LIMIT 1);
SET @unit_kor_listen   = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='KOR_LISTEN_MAIN'   LIMIT 1);

-- 과학 단원
SET @unit_sci_wave     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_PHYSICS_WAVE'  LIMIT 1);
SET @unit_sci_react    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_CHEM_REACT'    LIMIT 1);
SET @unit_sci_gene     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_BIO_GENETICS'  LIMIT 1);
SET @unit_sci_mech     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_PHYSICS_MECH'  LIMIT 1);
SET @unit_sci_cell     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_BIO_CELL'      LIMIT 1);
SET @unit_sci_strata   = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_EARTH_STRATA'  LIMIT 1);
SET @unit_sci_elec     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_PHYSICS_ELEC'  LIMIT 1);
SET @unit_sci_state    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_MATTER_STATE'  LIMIT 1);
SET @unit_sci_solar    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SCI_EARTH_SOLAR'   LIMIT 1);

-- 사회 단원
SET @unit_soc_const    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_LAW_CONSTITUTION'  LIMIT 1);
SET @unit_soc_trade    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_ECONOMY_TRADE'      LIMIT 1);
SET @unit_soc_climate  = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_GEO_WORLD_CLIMATE'  LIMIT 1);
SET @unit_soc_market   = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_ECONOMY_MARKET'     LIMIT 1);
SET @unit_soc_demo     = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_POLITICS_DEMO'      LIMIT 1);
SET @unit_soc_modern   = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_HIST_KOR_MOD'       LIMIT 1);
SET @unit_soc_elect    = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_POLITICS_ELECT'     LIMIT 1);
SET @unit_soc_ecobasic = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_ECONOMY_BASIC'      LIMIT 1);
SET @unit_soc_terrain  = (SELECT code_id FROM system_codes WHERE code_group='UNIT' AND code_value='SOC_GEO_KOR_TERRAIN'    LIMIT 1);

-- ============================================================
-- 문제 - 수학 (A레벨 5개)
-- ============================================================
INSERT INTO problems (subject_code_id, unit_code_id, level, grade, source, problem_type, question_text, answer, explanation, difficulty, estimated_time, is_active) VALUES
(@subj_math, @unit_math_eq2,    'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', 'x²-5x+6=0의 두 근의 합은?',            '1', '인수분해: (x-2)(x-3)=0, 근: 2,3, 합=5',  4, 120, 1),
(@subj_math, @unit_math_quad,   'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', 'y=2x²-4x+3의 꼭짓점 좌표는?',          '2', '표준형: y=2(x-1)²+1, 꼭짓점 (1,1)',       4, 120, 1),
(@subj_math, @unit_math_trig,   'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', 'sin30°+cos60°의 값은?',                '1', 'sin30°=1/2, cos60°=1/2, 합=1',            4, 120, 1),
(@subj_math, @unit_math_exp,    'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', 'log₂8의 값은?',                        '3', 'log₂8=log₂2³=3',                          4, 120, 1),
(@subj_math, @unit_math_seq,    'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '등차수열 2,5,8,11,...의 제10항은?',     '2', 'a₁=2, d=3, a₁₀=2+9×3=29',                4, 120, 1);

INSERT INTO problem_options (problem_id, option_no, option_text) VALUES
((SELECT problem_id FROM problems WHERE question_text='x²-5x+6=0의 두 근의 합은?'), 1, '5'),
((SELECT problem_id FROM problems WHERE question_text='x²-5x+6=0의 두 근의 합은?'), 2, '4'),
((SELECT problem_id FROM problems WHERE question_text='x²-5x+6=0의 두 근의 합은?'), 3, '6'),
((SELECT problem_id FROM problems WHERE question_text='x²-5x+6=0의 두 근의 합은?'), 4, '7'),
((SELECT problem_id FROM problems WHERE question_text='y=2x²-4x+3의 꼭짓점 좌표는?'), 1, '(1,2)'),
((SELECT problem_id FROM problems WHERE question_text='y=2x²-4x+3의 꼭짓점 좌표는?'), 2, '(1,1)'),
((SELECT problem_id FROM problems WHERE question_text='y=2x²-4x+3의 꼭짓점 좌표는?'), 3, '(2,1)'),
((SELECT problem_id FROM problems WHERE question_text='y=2x²-4x+3의 꼭짓점 좌표는?'), 4, '(-1,1)'),
((SELECT problem_id FROM problems WHERE question_text='sin30°+cos60°의 값은?'), 1, '1'),
((SELECT problem_id FROM problems WHERE question_text='sin30°+cos60°의 값은?'), 2, '0'),
((SELECT problem_id FROM problems WHERE question_text='sin30°+cos60°의 값은?'), 3, '2'),
((SELECT problem_id FROM problems WHERE question_text='sin30°+cos60°의 값은?'), 4, '½'),
((SELECT problem_id FROM problems WHERE question_text='log₂8의 값은?'), 1, '2'),
((SELECT problem_id FROM problems WHERE question_text='log₂8의 값은?'), 2, '4'),
((SELECT problem_id FROM problems WHERE question_text='log₂8의 값은?'), 3, '3'),
((SELECT problem_id FROM problems WHERE question_text='log₂8의 값은?'), 4, '1'),
((SELECT problem_id FROM problems WHERE question_text='등차수열 2,5,8,11,...의 제10항은?'), 1, '26'),
((SELECT problem_id FROM problems WHERE question_text='등차수열 2,5,8,11,...의 제10항은?'), 2, '29'),
((SELECT problem_id FROM problems WHERE question_text='등차수열 2,5,8,11,...의 제10항은?'), 3, '32'),
((SELECT problem_id FROM problems WHERE question_text='등차수열 2,5,8,11,...의 제10항은?'), 4, '23');

-- ============================================================
-- 문제 - 수학 (B레벨 5개)
-- ============================================================
INSERT INTO problems (subject_code_id, unit_code_id, level, grade, source, problem_type, question_text, answer, explanation, difficulty, estimated_time, is_active) VALUES
(@subj_math, @unit_math_linear, 'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', 'y=2x+3의 x절편은?',                    '2', 'y=0: 2x+3=0, x=-3/2',  3, 120, 1),
(@subj_math, @unit_math_sim,    'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', 'x+y=5, x-y=1의 해: x=?',               '1', '두 식 더하면 2x=6, x=3',3, 120, 1),
(@subj_math, @unit_math_area,   'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '밑변 6, 높이 4인 삼각형의 넓이는?',      '3', '넓이=1/2×6×4=12',       3, 120, 1),
(@subj_math, @unit_math_ratio,  'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '전체 60개 중 45개가 정답일 때 정답률은?','1', '45/60×100=75%',         3, 120, 1),
(@subj_math, @unit_math_expr,   'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '3x+2=11일 때 x의 값은?',                '2', '3x=9, x=3',             3, 120, 1);

INSERT INTO problem_options (problem_id, option_no, option_text) VALUES
((SELECT problem_id FROM problems WHERE question_text='y=2x+3의 x절편은?'), 1, '-2'),
((SELECT problem_id FROM problems WHERE question_text='y=2x+3의 x절편은?'), 2, '-3/2'),
((SELECT problem_id FROM problems WHERE question_text='y=2x+3의 x절편은?'), 3, '3/2'),
((SELECT problem_id FROM problems WHERE question_text='y=2x+3의 x절편은?'), 4, '2'),
((SELECT problem_id FROM problems WHERE question_text='x+y=5, x-y=1의 해: x=?'), 1, '3'),
((SELECT problem_id FROM problems WHERE question_text='x+y=5, x-y=1의 해: x=?'), 2, '2'),
((SELECT problem_id FROM problems WHERE question_text='x+y=5, x-y=1의 해: x=?'), 3, '4'),
((SELECT problem_id FROM problems WHERE question_text='x+y=5, x-y=1의 해: x=?'), 4, '5'),
((SELECT problem_id FROM problems WHERE question_text='밑변 6, 높이 4인 삼각형의 넓이는?'), 1, '10'),
((SELECT problem_id FROM problems WHERE question_text='밑변 6, 높이 4인 삼각형의 넓이는?'), 2, '14'),
((SELECT problem_id FROM problems WHERE question_text='밑변 6, 높이 4인 삼각형의 넓이는?'), 3, '12'),
((SELECT problem_id FROM problems WHERE question_text='밑변 6, 높이 4인 삼각형의 넓이는?'), 4, '8'),
((SELECT problem_id FROM problems WHERE question_text='전체 60개 중 45개가 정답일 때 정답률은?'), 1, '75%'),
((SELECT problem_id FROM problems WHERE question_text='전체 60개 중 45개가 정답일 때 정답률은?'), 2, '60%'),
((SELECT problem_id FROM problems WHERE question_text='전체 60개 중 45개가 정답일 때 정답률은?'), 3, '80%'),
((SELECT problem_id FROM problems WHERE question_text='전체 60개 중 45개가 정답일 때 정답률은?'), 4, '90%'),
((SELECT problem_id FROM problems WHERE question_text='3x+2=11일 때 x의 값은?'), 1, '2'),
((SELECT problem_id FROM problems WHERE question_text='3x+2=11일 때 x의 값은?'), 2, '3'),
((SELECT problem_id FROM problems WHERE question_text='3x+2=11일 때 x의 값은?'), 3, '4'),
((SELECT problem_id FROM problems WHERE question_text='3x+2=11일 때 x의 값은?'), 4, '1');

-- ============================================================
-- 문제 - 수학 (C레벨 5개)
-- ============================================================
INSERT INTO problems (subject_code_id, unit_code_id, level, grade, source, problem_type, question_text, answer, explanation, difficulty, estimated_time, is_active) VALUES
(@subj_math, @unit_math_int,    'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '(-3)+(-5)의 값은?',             '3', '음수끼리의 덧셈: -(3+5)=-8', 2, 120, 1),
(@subj_math, @unit_math_factor, 'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '12를 소인수분해하면?',           '1', '12=2²×3',                    2, 120, 1),
(@subj_math, @unit_math_arith,  'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '3×4+2의 값은?',                 '2', '곱셈 먼저: 12+2=14',          2, 120, 1),
(@subj_math, @unit_math_divis,  'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '12의 약수의 개수는?',           '1', '1,2,3,4,6,12 → 6개',         2, 120, 1),
(@subj_math, @unit_math_shape,  'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '직각삼각형에서 직각의 크기는?', '2', '직각=90°',                    2, 120, 1);

INSERT INTO problem_options (problem_id, option_no, option_text) VALUES
((SELECT problem_id FROM problems WHERE question_text='(-3)+(-5)의 값은?'), 1, '-2'),
((SELECT problem_id FROM problems WHERE question_text='(-3)+(-5)의 값은?'), 2, '8'),
((SELECT problem_id FROM problems WHERE question_text='(-3)+(-5)의 값은?'), 3, '-8'),
((SELECT problem_id FROM problems WHERE question_text='(-3)+(-5)의 값은?'), 4, '2'),
((SELECT problem_id FROM problems WHERE question_text='12를 소인수분해하면?'), 1, '2²×3'),
((SELECT problem_id FROM problems WHERE question_text='12를 소인수분해하면?'), 2, '2×3²'),
((SELECT problem_id FROM problems WHERE question_text='12를 소인수분해하면?'), 3, '2³×3'),
((SELECT problem_id FROM problems WHERE question_text='12를 소인수분해하면?'), 4, '2×3'),
((SELECT problem_id FROM problems WHERE question_text='3×4+2의 값은?'), 1, '10'),
((SELECT problem_id FROM problems WHERE question_text='3×4+2의 값은?'), 2, '14'),
((SELECT problem_id FROM problems WHERE question_text='3×4+2의 값은?'), 3, '16'),
((SELECT problem_id FROM problems WHERE question_text='3×4+2의 값은?'), 4, '12'),
((SELECT problem_id FROM problems WHERE question_text='12의 약수의 개수는?'), 1, '6'),
((SELECT problem_id FROM problems WHERE question_text='12의 약수의 개수는?'), 2, '4'),
((SELECT problem_id FROM problems WHERE question_text='12의 약수의 개수는?'), 3, '8'),
((SELECT problem_id FROM problems WHERE question_text='12의 약수의 개수는?'), 4, '5'),
((SELECT problem_id FROM problems WHERE question_text='직각삼각형에서 직각의 크기는?'), 1, '45°'),
((SELECT problem_id FROM problems WHERE question_text='직각삼각형에서 직각의 크기는?'), 2, '90°'),
((SELECT problem_id FROM problems WHERE question_text='직각삼각형에서 직각의 크기는?'), 3, '60°'),
((SELECT problem_id FROM problems WHERE question_text='직각삼각형에서 직각의 크기는?'), 4, '180°');

-- ============================================================
-- 문제 - 영어 (A/B/C 각 3개씩)
-- ============================================================
INSERT INTO problems (subject_code_id, unit_code_id, level, grade, source, problem_type, question_text, answer, explanation, difficulty, estimated_time, is_active) VALUES
(@subj_eng, @unit_eng_infer,  'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '밑줄 친 "it" 이 가리키는 것으로 가장 적절한 것은?',                              '2', '문맥상 앞 문장의 주어를 받는 대명사',         4, 120, 1),
(@subj_eng, @unit_eng_struct, 'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '주어진 단어를 올바르게 배열한 것은? [has / she / studied / long]',               '1', 'She has studied long.',                        4, 120, 1),
(@subj_eng, @unit_eng_main,   'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '다음 글의 요지로 가장 적절한 것은? (환경 보호 관련 지문)',                        '3', '글의 핵심 주장을 파악하는 문제',              4, 120, 1),
(@subj_eng, @unit_eng_blank,  'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', 'She was _____ by the beauty of the scenery. 빈칸에 알맞은 말은?',               '2', 'amazed가 문맥에 가장 적합',                   3, 120, 1),
(@subj_eng, @unit_eng_rel,    'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', 'The book _____ I bought yesterday is interesting. 빈칸에 알맞은 것은?',           '1', '선행사가 사물이므로 which 또는 that',          3, 120, 1),
(@subj_eng, @unit_eng_topic,  'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '다음 글의 주제로 가장 적절한 것은? (SNS 중독 관련 지문)',                         '3', '지문의 중심 소재를 파악하는 문제',             3, 120, 1),
(@subj_eng, @unit_eng_tense,  'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', 'She _____ English for three years. 올바른 시제를 고르시오.',                     '2', '현재완료: has studied',                        2, 120, 1),
(@subj_eng, @unit_eng_syn,    'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '"abundant"와 의미가 가장 유사한 단어는?',                                        '1', 'plentiful(풍부한)이 동의어',                  2, 120, 1),
(@subj_eng, @unit_eng_prep,   'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', 'I will meet you _____ Monday morning. 빈칸에 알맞은 전치사는?',                  '3', 'on + 요일',                                   2, 120, 1);

INSERT INTO problem_options (problem_id, option_no, option_text) VALUES
((SELECT problem_id FROM problems WHERE question_text LIKE '%밑줄 친 "it" 이 가리키는%'), 1, '저자'),((SELECT problem_id FROM problems WHERE question_text LIKE '%밑줄 친 "it" 이 가리키는%'), 2, '기술의 발전'),((SELECT problem_id FROM problems WHERE question_text LIKE '%밑줄 친 "it" 이 가리키는%'), 3, '환경 문제'),((SELECT problem_id FROM problems WHERE question_text LIKE '%밑줄 친 "it" 이 가리키는%'), 4, '독자'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%has / she / studied%'), 1, 'She has studied long.'),((SELECT problem_id FROM problems WHERE question_text LIKE '%has / she / studied%'), 2, 'Has she long studied.'),((SELECT problem_id FROM problems WHERE question_text LIKE '%has / she / studied%'), 3, 'Long she has studied.'),((SELECT problem_id FROM problems WHERE question_text LIKE '%has / she / studied%'), 4, 'She studied has long.'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%환경 보호 관련 지문%'), 1, '경제 성장의 필요성'),((SELECT problem_id FROM problems WHERE question_text LIKE '%환경 보호 관련 지문%'), 2, '기술 혁신의 중요성'),((SELECT problem_id FROM problems WHERE question_text LIKE '%환경 보호 관련 지문%'), 3, '환경 보호를 위한 개인의 노력'),((SELECT problem_id FROM problems WHERE question_text LIKE '%환경 보호 관련 지문%'), 4, '도시화의 문제점'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%beauty of the scenery%'), 1, 'bored'),((SELECT problem_id FROM problems WHERE question_text LIKE '%beauty of the scenery%'), 2, 'amazed'),((SELECT problem_id FROM problems WHERE question_text LIKE '%beauty of the scenery%'), 3, 'confused'),((SELECT problem_id FROM problems WHERE question_text LIKE '%beauty of the scenery%'), 4, 'tired'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%I bought yesterday is interesting%'), 1, 'that'),((SELECT problem_id FROM problems WHERE question_text LIKE '%I bought yesterday is interesting%'), 2, 'who'),((SELECT problem_id FROM problems WHERE question_text LIKE '%I bought yesterday is interesting%'), 3, 'whom'),((SELECT problem_id FROM problems WHERE question_text LIKE '%I bought yesterday is interesting%'), 4, 'whose'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%SNS 중독%'), 1, 'SNS의 긍정적 효과'),((SELECT problem_id FROM problems WHERE question_text LIKE '%SNS 중독%'), 2, '스마트폰 보급률'),((SELECT problem_id FROM problems WHERE question_text LIKE '%SNS 중독%'), 3, 'SNS 과의존의 문제'),((SELECT problem_id FROM problems WHERE question_text LIKE '%SNS 중독%'), 4, '인터넷 속도의 발전'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%She _____ English for three years%'), 1, 'study'),((SELECT problem_id FROM problems WHERE question_text LIKE '%She _____ English for three years%'), 2, 'has studied'),((SELECT problem_id FROM problems WHERE question_text LIKE '%She _____ English for three years%'), 3, 'studied'),((SELECT problem_id FROM problems WHERE question_text LIKE '%She _____ English for three years%'), 4, 'will study'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%abundant%'), 1, 'plentiful'),((SELECT problem_id FROM problems WHERE question_text LIKE '%abundant%'), 2, 'scarce'),((SELECT problem_id FROM problems WHERE question_text LIKE '%abundant%'), 3, 'ancient'),((SELECT problem_id FROM problems WHERE question_text LIKE '%abundant%'), 4, 'rapid'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%Monday morning%'), 1, 'in'),((SELECT problem_id FROM problems WHERE question_text LIKE '%Monday morning%'), 2, 'at'),((SELECT problem_id FROM problems WHERE question_text LIKE '%Monday morning%'), 3, 'on'),((SELECT problem_id FROM problems WHERE question_text LIKE '%Monday morning%'), 4, 'by');

-- ============================================================
-- 문제 - 국어 (A/B/C 각 3개씩)
-- ============================================================
INSERT INTO problems (subject_code_id, unit_code_id, level, grade, source, problem_type, question_text, answer, explanation, difficulty, estimated_time, is_active) VALUES
(@subj_kor, @unit_kor_infer,  'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '윗글에서 추론할 수 있는 내용으로 가장 적절한 것은? (과학기술 지문)',        '2', '글의 논리적 흐름에서 도출 가능한 내용',       4, 120, 1),
(@subj_kor, @unit_kor_debate, 'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '다음 토론에서 찬성 측 반론으로 가장 적절한 것은?',                         '3', '토론 전략: 상대방 논거 약화',                  4, 120, 1),
(@subj_kor, @unit_kor_novel,  'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '윗글의 서술 방식에 대한 설명으로 적절하지 않은 것은?',                      '4', '1인칭 관찰자 시점 확인',                       4, 120, 1),
(@subj_kor, @unit_kor_fact,   'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '다음 글의 내용과 일치하는 것은? (경제 지문)',                               '1', '지문 내용과 선택지 일치 여부 판단',             3, 120, 1),
(@subj_kor, @unit_kor_poem,   'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '다음 시에서 화자의 정서로 가장 적절한 것은?',                               '2', '시어와 분위기를 통해 화자의 감정 파악',         3, 120, 1),
(@subj_kor, @unit_kor_sent,   'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '다음 문장에서 목적어에 해당하는 것은? "나는 사과를 먹었다."',               '3', '"사과를"이 목적어',                             3, 120, 1),
(@subj_kor, @unit_kor_parts,  'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '다음 중 명사가 아닌 것은?',                                                 '2', '형용사와 명사 구분',                           2, 120, 1),
(@subj_kor, @unit_kor_cls,    'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '다음 고전 시조에서 초장의 중심 소재는?',                                    '1', '시조의 구조와 소재 파악',                      2, 120, 1),
(@subj_kor, @unit_kor_listen, 'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '다음 강연의 중심 내용으로 적절한 것은?',                                    '3', '강연의 핵심 메시지 파악',                      2, 120, 1);

INSERT INTO problem_options (problem_id, option_no, option_text) VALUES
((SELECT problem_id FROM problems WHERE question_text LIKE '%과학기술 지문%'), 1, '기술 발전은 항상 사회에 이롭다'),((SELECT problem_id FROM problems WHERE question_text LIKE '%과학기술 지문%'), 2, '기술 발전의 속도 조절이 필요하다'),((SELECT problem_id FROM problems WHERE question_text LIKE '%과학기술 지문%'), 3, '과학과 인문학은 무관하다'),((SELECT problem_id FROM problems WHERE question_text LIKE '%과학기술 지문%'), 4, '기술은 경제 성장만을 위한 것이다'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%찬성 측 반론%'), 1, '상대 측 주장을 그대로 인정한다'),((SELECT problem_id FROM problems WHERE question_text LIKE '%찬성 측 반론%'), 2, '관련 없는 사례를 든다'),((SELECT problem_id FROM problems WHERE question_text LIKE '%찬성 측 반론%'), 3, '상대 측 근거의 타당성을 반박한다'),((SELECT problem_id FROM problems WHERE question_text LIKE '%찬성 측 반론%'), 4, '감정적 호소로 설득한다'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%서술 방식에 대한 설명%'), 1, '1인칭 주인공 시점이다'),((SELECT problem_id FROM problems WHERE question_text LIKE '%서술 방식에 대한 설명%'), 2, '과거와 현재가 교차된다'),((SELECT problem_id FROM problems WHERE question_text LIKE '%서술 방식에 대한 설명%'), 3, '사건을 시간 순서대로 서술한다'),((SELECT problem_id FROM problems WHERE question_text LIKE '%서술 방식에 대한 설명%'), 4, '전지적 시점에서 내면을 묘사한다'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%경제 지문%'), 1, '글의 내용과 일치하는 선택지'),((SELECT problem_id FROM problems WHERE question_text LIKE '%경제 지문%'), 2, '가격이 오르면 수요가 증가한다'),((SELECT problem_id FROM problems WHERE question_text LIKE '%경제 지문%'), 3, '공급이 늘면 가격이 상승한다'),((SELECT problem_id FROM problems WHERE question_text LIKE '%경제 지문%'), 4, '수요와 공급은 무관하다'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%화자의 정서%'), 1, '기쁨과 흥분'),((SELECT problem_id FROM problems WHERE question_text LIKE '%화자의 정서%'), 2, '그리움과 슬픔'),((SELECT problem_id FROM problems WHERE question_text LIKE '%화자의 정서%'), 3, '분노와 저항'),((SELECT problem_id FROM problems WHERE question_text LIKE '%화자의 정서%'), 4, '평온과 만족'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%나는 사과를 먹었다%'), 1, '나는'),((SELECT problem_id FROM problems WHERE question_text LIKE '%나는 사과를 먹었다%'), 2, '는'),((SELECT problem_id FROM problems WHERE question_text LIKE '%나는 사과를 먹었다%'), 3, '사과를'),((SELECT problem_id FROM problems WHERE question_text LIKE '%나는 사과를 먹었다%'), 4, '먹었다'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%명사가 아닌 것%'), 1, '사랑'),((SELECT problem_id FROM problems WHERE question_text LIKE '%명사가 아닌 것%'), 2, '아름다운'),((SELECT problem_id FROM problems WHERE question_text LIKE '%명사가 아닌 것%'), 3, '학교'),((SELECT problem_id FROM problems WHERE question_text LIKE '%명사가 아닌 것%'), 4, '행복'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%시조에서 초장%'), 1, '자연'),((SELECT problem_id FROM problems WHERE question_text LIKE '%시조에서 초장%'), 2, '임금'),((SELECT problem_id FROM problems WHERE question_text LIKE '%시조에서 초장%'), 3, '전쟁'),((SELECT problem_id FROM problems WHERE question_text LIKE '%시조에서 초장%'), 4, '죽음'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%강연의 중심 내용%'), 1, '경제 성장 방법'),((SELECT problem_id FROM problems WHERE question_text LIKE '%강연의 중심 내용%'), 2, '역사적 사건 정리'),((SELECT problem_id FROM problems WHERE question_text LIKE '%강연의 중심 내용%'), 3, '환경 보전의 중요성'),((SELECT problem_id FROM problems WHERE question_text LIKE '%강연의 중심 내용%'), 4, '스포츠의 가치');

-- ============================================================
-- 문제 - 과학 (A/B/C 각 3개씩)
-- ============================================================
INSERT INTO problems (subject_code_id, unit_code_id, level, grade, source, problem_type, question_text, answer, explanation, difficulty, estimated_time, is_active) VALUES
(@subj_sci, @unit_sci_wave,   'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '파장 2m, 진동수 5Hz인 파동의 속도는?',                     '3', 'v=fλ=5×2=10 m/s',               4, 120, 1),
(@subj_sci, @unit_sci_react,  'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '2H₂+O₂→? 반응의 생성물은?',                               '1', '2H₂O (물 생성)',                 4, 120, 1),
(@subj_sci, @unit_sci_gene,   'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', 'AaBb × AaBb 교배 시 AABB 자손의 비율은?',                  '2', '1/4 × 1/4 = 1/16',              4, 120, 1),
(@subj_sci, @unit_sci_mech,   'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '질량 5kg인 물체에 10N의 힘이 작용할 때 가속도는?',           '2', 'F=ma, a=F/m=10/5=2 m/s²',       3, 120, 1),
(@subj_sci, @unit_sci_cell,   'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '세포 분열 시 DNA 복제가 일어나는 단계는?',                  '1', '간기(S기)에 DNA 복제 발생',      3, 120, 1),
(@subj_sci, @unit_sci_strata, 'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '지층의 역전 현상이 발생하는 원인은?',                       '3', '지각 변동(습곡, 단층)에 의해 역전',3, 120, 1),
(@subj_sci, @unit_sci_elec,   'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '저항 4Ω, 전압 12V일 때 전류는?',                           '2', 'I=V/R=12/4=3A',                  2, 120, 1),
(@subj_sci, @unit_sci_state,  'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '액체가 기체로 변하는 현상을 무엇이라 하는가?',               '1', '기화(증발)',                      2, 120, 1),
(@subj_sci, @unit_sci_solar,  'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '태양계에서 가장 큰 행성은?',                                '3', '목성이 태양계 최대 행성',         2, 120, 1);

INSERT INTO problem_options (problem_id, option_no, option_text) VALUES
((SELECT problem_id FROM problems WHERE question_text LIKE '%파장 2m, 진동수 5Hz%'), 1, '2 m/s'),((SELECT problem_id FROM problems WHERE question_text LIKE '%파장 2m, 진동수 5Hz%'), 2, '5 m/s'),((SELECT problem_id FROM problems WHERE question_text LIKE '%파장 2m, 진동수 5Hz%'), 3, '10 m/s'),((SELECT problem_id FROM problems WHERE question_text LIKE '%파장 2m, 진동수 5Hz%'), 4, '7 m/s'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%2H₂+O₂%'), 1, '2H₂O'),((SELECT problem_id FROM problems WHERE question_text LIKE '%2H₂+O₂%'), 2, 'H₂O₂'),((SELECT problem_id FROM problems WHERE question_text LIKE '%2H₂+O₂%'), 3, 'H₂O'),((SELECT problem_id FROM problems WHERE question_text LIKE '%2H₂+O₂%'), 4, 'O₃'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%AaBb × AaBb%'), 1, '1/4'),((SELECT problem_id FROM problems WHERE question_text LIKE '%AaBb × AaBb%'), 2, '1/16'),((SELECT problem_id FROM problems WHERE question_text LIKE '%AaBb × AaBb%'), 3, '1/8'),((SELECT problem_id FROM problems WHERE question_text LIKE '%AaBb × AaBb%'), 4, '1/2'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%질량 5kg인 물체에 10N%'), 1, '1 m/s²'),((SELECT problem_id FROM problems WHERE question_text LIKE '%질량 5kg인 물체에 10N%'), 2, '2 m/s²'),((SELECT problem_id FROM problems WHERE question_text LIKE '%질량 5kg인 물체에 10N%'), 3, '5 m/s²'),((SELECT problem_id FROM problems WHERE question_text LIKE '%질량 5kg인 물체에 10N%'), 4, '50 m/s²'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%DNA 복제가 일어나는 단계%'), 1, '간기'),((SELECT problem_id FROM problems WHERE question_text LIKE '%DNA 복제가 일어나는 단계%'), 2, '전기'),((SELECT problem_id FROM problems WHERE question_text LIKE '%DNA 복제가 일어나는 단계%'), 3, '중기'),((SELECT problem_id FROM problems WHERE question_text LIKE '%DNA 복제가 일어나는 단계%'), 4, '말기'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%지층의 역전%'), 1, '해수면 변화'),((SELECT problem_id FROM problems WHERE question_text LIKE '%지층의 역전%'), 2, '퇴적 속도 차이'),((SELECT problem_id FROM problems WHERE question_text LIKE '%지층의 역전%'), 3, '지각 변동'),((SELECT problem_id FROM problems WHERE question_text LIKE '%지층의 역전%'), 4, '화산 폭발'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%저항 4Ω, 전압 12V%'), 1, '1A'),((SELECT problem_id FROM problems WHERE question_text LIKE '%저항 4Ω, 전압 12V%'), 2, '3A'),((SELECT problem_id FROM problems WHERE question_text LIKE '%저항 4Ω, 전압 12V%'), 3, '4A'),((SELECT problem_id FROM problems WHERE question_text LIKE '%저항 4Ω, 전압 12V%'), 4, '48A'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%액체가 기체로 변하는%'), 1, '기화'),((SELECT problem_id FROM problems WHERE question_text LIKE '%액체가 기체로 변하는%'), 2, '응결'),((SELECT problem_id FROM problems WHERE question_text LIKE '%액체가 기체로 변하는%'), 3, '융해'),((SELECT problem_id FROM problems WHERE question_text LIKE '%액체가 기체로 변하는%'), 4, '응고'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%가장 큰 행성%'), 1, '토성'),((SELECT problem_id FROM problems WHERE question_text LIKE '%가장 큰 행성%'), 2, '천왕성'),((SELECT problem_id FROM problems WHERE question_text LIKE '%가장 큰 행성%'), 3, '목성'),((SELECT problem_id FROM problems WHERE question_text LIKE '%가장 큰 행성%'), 4, '해왕성');

-- ============================================================
-- 문제 - 사회 (A/B/C 각 3개씩)
-- ============================================================
INSERT INTO problems (subject_code_id, unit_code_id, level, grade, source, problem_type, question_text, answer, explanation, difficulty, estimated_time, is_active) VALUES
(@subj_soc, @unit_soc_const,    'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '기본권 제한의 한계를 규정한 헌법 조항의 내용으로 옳은 것은?',          '2', '헌법 제37조 2항: 필요 최소한의 범위 제한',    4, 120, 1),
(@subj_soc, @unit_soc_trade,    'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '비교 우위론에 기반한 자유 무역의 효과로 옳은 것은?',                   '3', '특화를 통한 생산 효율 향상',                   4, 120, 1),
(@subj_soc, @unit_soc_climate,  'A', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '지중해성 기후의 특징으로 옳은 것은?',                                  '1', '여름 건조, 겨울 온난 다우',                    4, 120, 1),
(@subj_soc, @unit_soc_market,   'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '수요 증가 시 균형 가격과 균형 거래량의 변화로 옳은 것은?',             '1', '수요 증가 → 가격 상승, 거래량 증가',           3, 120, 1),
(@subj_soc, @unit_soc_demo,     'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '직접 민주주의의 사례로 가장 적절한 것은?',                             '3', '국민 투표, 국민 발안 등',                       3, 120, 1),
(@subj_soc, @unit_soc_modern,   'B', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '3·1 운동(1919)의 결과로 나타난 것은?',                                '2', '대한민국 임시정부 수립',                       3, 120, 1),
(@subj_soc, @unit_soc_elect,    'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '민주 선거의 4원칙에 해당하지 않는 것은?',                              '3', '보통·평등·직접·비밀 선거가 4원칙',              2, 120, 1),
(@subj_soc, @unit_soc_ecobasic, 'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '희소성의 의미로 가장 적절한 것은?',                                    '1', '인간의 욕구에 비해 자원이 부족한 상태',         2, 120, 1),
(@subj_soc, @unit_soc_terrain,  'C', 'GRADE_2', 'CUSTOM', 'MULTIPLE_CHOICE', '우리나라의 지형적 특징으로 옳은 것은?',                                '2', '동고서저의 경동 지형',                          2, 120, 1);

INSERT INTO problem_options (problem_id, option_no, option_text) VALUES
((SELECT problem_id FROM problems WHERE question_text LIKE '%기본권 제한의 한계%'), 1, '기본권은 어떠한 경우에도 제한할 수 없다'),((SELECT problem_id FROM problems WHERE question_text LIKE '%기본권 제한의 한계%'), 2, '국가 안전보장 등을 위해 필요 최소한으로 제한 가능'),((SELECT problem_id FROM problems WHERE question_text LIKE '%기본권 제한의 한계%'), 3, '국회 의결만으로 제한 가능'),((SELECT problem_id FROM problems WHERE question_text LIKE '%기본권 제한의 한계%'), 4, '대통령령으로 자유롭게 제한 가능'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%비교 우위론%'), 1, '모든 나라가 동일한 상품을 생산'),((SELECT problem_id FROM problems WHERE question_text LIKE '%비교 우위론%'), 2, '무역 적자 국가만 이익'),((SELECT problem_id FROM problems WHERE question_text LIKE '%비교 우위론%'), 3, '각국이 비교 우위 상품에 특화하여 상호 이익'),((SELECT problem_id FROM problems WHERE question_text LIKE '%비교 우위론%'), 4, '자국 산업 보호가 최우선'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%지중해성 기후%'), 1, '여름 건조, 겨울 온난 다우'),((SELECT problem_id FROM problems WHERE question_text LIKE '%지중해성 기후%'), 2, '연중 고온 다우'),((SELECT problem_id FROM problems WHERE question_text LIKE '%지중해성 기후%'), 3, '연중 한랭 건조'),((SELECT problem_id FROM problems WHERE question_text LIKE '%지중해성 기후%'), 4, '여름 다우, 겨울 건조'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%수요 증가 시 균형%'), 1, '가격 상승, 거래량 증가'),((SELECT problem_id FROM problems WHERE question_text LIKE '%수요 증가 시 균형%'), 2, '가격 하락, 거래량 감소'),((SELECT problem_id FROM problems WHERE question_text LIKE '%수요 증가 시 균형%'), 3, '가격 상승, 거래량 감소'),((SELECT problem_id FROM problems WHERE question_text LIKE '%수요 증가 시 균형%'), 4, '가격 불변, 거래량 증가'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%직접 민주주의의 사례%'), 1, '국회의원 선거'),((SELECT problem_id FROM problems WHERE question_text LIKE '%직접 민주주의의 사례%'), 2, '대통령 선거'),((SELECT problem_id FROM problems WHERE question_text LIKE '%직접 민주주의의 사례%'), 3, '국민 투표'),((SELECT problem_id FROM problems WHERE question_text LIKE '%직접 민주주의의 사례%'), 4, '지방 의회'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%3·1 운동%'), 1, '을사늑약 체결'),((SELECT problem_id FROM problems WHERE question_text LIKE '%3·1 운동%'), 2, '대한민국 임시정부 수립'),((SELECT problem_id FROM problems WHERE question_text LIKE '%3·1 운동%'), 3, '강화도 조약 체결'),((SELECT problem_id FROM problems WHERE question_text LIKE '%3·1 운동%'), 4, '국권 피탈'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%민주 선거의 4원칙%'), 1, '보통 선거'),((SELECT problem_id FROM problems WHERE question_text LIKE '%민주 선거의 4원칙%'), 2, '평등 선거'),((SELECT problem_id FROM problems WHERE question_text LIKE '%민주 선거의 4원칙%'), 3, '공개 선거'),((SELECT problem_id FROM problems WHERE question_text LIKE '%민주 선거의 4원칙%'), 4, '비밀 선거'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%희소성의 의미%'), 1, '인간의 욕구에 비해 자원이 부족한 상태'),((SELECT problem_id FROM problems WHERE question_text LIKE '%희소성의 의미%'), 2, '자원이 전혀 없는 상태'),((SELECT problem_id FROM problems WHERE question_text LIKE '%희소성의 의미%'), 3, '물건의 가격이 매우 비싼 상태'),((SELECT problem_id FROM problems WHERE question_text LIKE '%희소성의 의미%'), 4, '공급이 수요보다 많은 상태'),
((SELECT problem_id FROM problems WHERE question_text LIKE '%우리나라의 지형적 특징%'), 1, '서고동저 지형'),((SELECT problem_id FROM problems WHERE question_text LIKE '%우리나라의 지형적 특징%'), 2, '동고서저 경동 지형'),((SELECT problem_id FROM problems WHERE question_text LIKE '%우리나라의 지형적 특징%'), 3, '전체가 평야 지대'),((SELECT problem_id FROM problems WHERE question_text LIKE '%우리나라의 지형적 특징%'), 4, '북쪽이 낮고 남쪽이 높다');

-- ============================================================
-- 과제
-- ============================================================
INSERT INTO assignments (teacher_id, school_id, title, description, target_type, target_level, due_date, is_auto_assign, notify_email) VALUES
-- 완료 (due_date 과거)
(1, 1, '3월 A레벨 과제',        'A레벨 학생 대상 3월 정기 과제입니다.',     'LEVEL',   'A',   '2026-03-05 23:59:59', 0, 0),
(1, 1, '3월 B레벨 과제',        'B레벨 학생 대상 3월 정기 과제입니다.',     'LEVEL',   'B',   '2026-03-05 23:59:59', 0, 0),
(1, 1, '3월 C레벨 과제',        'C레벨 학생 대상 3월 정기 과제입니다.',     'LEVEL',   'C',   '2026-03-05 23:59:59', 0, 0),
(1, 1, '수학 1단원 복습',        '1단원 수와 연산 복습 과제입니다.',          'INDIVIDUAL', 'B',   '2026-03-10 23:59:59', 0, 0),
(1, 1, '영어 독해 연습 Unit 1',  '기초 독해 유닛 1 과제입니다.',             'INDIVIDUAL', 'A',   '2026-03-08 23:59:59', 0, 0),
-- 진행 중 (due_date 미래)
(1, 1, '수학 함수 심화 문제',    '함수 파트 심화 연습 과제입니다.',           'LEVEL',   'A',   '2026-03-25 23:59:59', 0, 0),
(1, 1, '국어 문법 기초 과제',    '문법 기초 단원 과제입니다.',                'INDIVIDUAL', 'B',   '2026-03-28 23:59:59', 0, 0),
(1, 1, '영어 독해 연습 Unit 3',  '독해 유닛 3 과제입니다.',                  'LEVEL',   'ALL', '2026-04-01 23:59:59', 0, 0),
(1, 1, '과학 힘과 운동 정리',    '물리 파트 힘과 운동 단원 과제입니다.',      'INDIVIDUAL', 'A',   '2026-04-05 23:59:59', 0, 0),
-- 초안 (due_date NULL)
(1, 1, '사회 지리 파트 과제',    '지리 파트 과제입니다. 준비 중.',            'LEVEL',   'B',   NULL,                  0, 0),
(1, 1, '영어 문법 총정리',       '전 범위 문법 총정리 과제입니다. 준비 중.',  'LEVEL',   'ALL', NULL,                  0, 0);

-- 과제 변수
SET @aid1  = (SELECT assignment_id FROM assignments WHERE title='3월 A레벨 과제'       LIMIT 1);
SET @aid2  = (SELECT assignment_id FROM assignments WHERE title='3월 B레벨 과제'       LIMIT 1);
SET @aid3  = (SELECT assignment_id FROM assignments WHERE title='3월 C레벨 과제'       LIMIT 1);
SET @aid4  = (SELECT assignment_id FROM assignments WHERE title='수학 1단원 복습'       LIMIT 1);
SET @aid5  = (SELECT assignment_id FROM assignments WHERE title='영어 독해 연습 Unit 1' LIMIT 1);
SET @aid6  = (SELECT assignment_id FROM assignments WHERE title='수학 함수 심화 문제'   LIMIT 1);
SET @aid7  = (SELECT assignment_id FROM assignments WHERE title='국어 문법 기초 과제'   LIMIT 1);
SET @aid8  = (SELECT assignment_id FROM assignments WHERE title='영어 독해 연습 Unit 3' LIMIT 1);
SET @aid9  = (SELECT assignment_id FROM assignments WHERE title='과학 힘과 운동 정리'   LIMIT 1);

-- 학생 변수
SET @s_a = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_a@test.com');
SET @s_b = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_b@test.com');
SET @s_c = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_c@test.com');
SET @s_d = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_d@test.com');
SET @s_e = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_e@test.com');
SET @s_f = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_f@test.com');
SET @s_g = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_g@test.com');
SET @s_h = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_h@test.com');

-- 과제 문제 연결 (완료 과제: 문제 있음 / 진행중: 문제 있음 / 초안: 문제 없음)
INSERT INTO assignment_problems (assignment_id, problem_id, order_no)
SELECT @aid1, problem_id, ROW_NUMBER() OVER (ORDER BY problem_id) FROM problems WHERE level='A';

INSERT INTO assignment_problems (assignment_id, problem_id, order_no)
SELECT @aid2, problem_id, ROW_NUMBER() OVER (ORDER BY problem_id) FROM problems WHERE level='B';

INSERT INTO assignment_problems (assignment_id, problem_id, order_no)
SELECT @aid3, problem_id, ROW_NUMBER() OVER (ORDER BY problem_id) FROM problems WHERE level='C';

INSERT INTO assignment_problems (assignment_id, problem_id, order_no)
SELECT @aid4, problem_id, ROW_NUMBER() OVER (ORDER BY problem_id) FROM problems WHERE level='B' LIMIT 5;

INSERT INTO assignment_problems (assignment_id, problem_id, order_no)
SELECT @aid5, problem_id, ROW_NUMBER() OVER (ORDER BY problem_id) FROM problems WHERE level='A' LIMIT 5;

INSERT INTO assignment_problems (assignment_id, problem_id, order_no)
SELECT @aid6, problem_id, ROW_NUMBER() OVER (ORDER BY problem_id) FROM problems WHERE level='A';

INSERT INTO assignment_problems (assignment_id, problem_id, order_no)
SELECT @aid7, problem_id, ROW_NUMBER() OVER (ORDER BY problem_id) FROM problems WHERE level='B';

INSERT INTO assignment_problems (assignment_id, problem_id, order_no)
SELECT @aid8, problem_id, ROW_NUMBER() OVER (ORDER BY problem_id)
FROM problems WHERE level IN ('A','B','C') AND subject_code_id=@subj_eng;

INSERT INTO assignment_problems (assignment_id, problem_id, order_no)
SELECT @aid9, problem_id, ROW_NUMBER() OVER (ORDER BY problem_id)
FROM problems WHERE level='A' AND subject_code_id=@subj_sci;

-- 과제 대상 학생
-- aid1: A레벨 4명 (a, d, f)
INSERT INTO assignment_targets (assignment_id, student_id) VALUES
(@aid1,@s_a),(@aid1,@s_d),(@aid1,@s_f);
-- aid2: B레벨 3명 (b, e, h)
INSERT INTO assignment_targets (assignment_id, student_id) VALUES
(@aid2,@s_b),(@aid2,@s_e),(@aid2,@s_h);
-- aid3: C레벨 2명 (c, g)
INSERT INTO assignment_targets (assignment_id, student_id) VALUES
(@aid3,@s_c),(@aid3,@s_g);
-- aid4: 5명 (b,c,e,g,h)
INSERT INTO assignment_targets (assignment_id, student_id) VALUES
(@aid4,@s_b),(@aid4,@s_c),(@aid4,@s_e),(@aid4,@s_g),(@aid4,@s_h);
-- aid5: 4명 (a,d,f,b)
INSERT INTO assignment_targets (assignment_id, student_id) VALUES
(@aid5,@s_a),(@aid5,@s_d),(@aid5,@s_f),(@aid5,@s_b);
-- aid6: A레벨 3명
INSERT INTO assignment_targets (assignment_id, student_id) VALUES
(@aid6,@s_a),(@aid6,@s_d),(@aid6,@s_f);
-- aid7: B레벨 4명
INSERT INTO assignment_targets (assignment_id, student_id) VALUES
(@aid7,@s_b),(@aid7,@s_e),(@aid7,@s_h),(@aid7,@s_c);
-- aid8: 전체 6명
INSERT INTO assignment_targets (assignment_id, student_id) VALUES
(@aid8,@s_a),(@aid8,@s_b),(@aid8,@s_c),(@aid8,@s_d),(@aid8,@s_e),(@aid8,@s_f);
-- aid9: A레벨 3명
INSERT INTO assignment_targets (assignment_id, student_id) VALUES
(@aid9,@s_a),(@aid9,@s_d),(@aid9,@s_f);

-- ============================================================
-- 학습 세션 (과제별 완료율 다양하게)
-- ============================================================
INSERT INTO learning_sessions (student_id, assignment_id, session_type, status, total_problems, solved_count, correct_count, completion_rate, started_at, completed_at) VALUES
-- aid1 (완료, 3/3명): a,d,f 모두 완료 → 완료율 100%
(@s_a, @aid1, 'ASSIGNMENT', 'COMPLETED', 15, 15, 12, 100.00, '2026-03-01 10:00:00', '2026-03-01 11:00:00'),
(@s_d, @aid1, 'ASSIGNMENT', 'COMPLETED', 15, 15, 15, 100.00, '2026-03-01 13:00:00', '2026-03-01 14:00:00'),
(@s_f, @aid1, 'ASSIGNMENT', 'COMPLETED', 15, 15, 10, 100.00, '2026-03-02 09:00:00', '2026-03-02 10:00:00'),
-- aid2 (완료, 2/3명): b,e 완료, h 미완 → 완료율 67%
(@s_b, @aid2, 'ASSIGNMENT', 'COMPLETED', 15, 15,  9, 100.00, '2026-03-01 10:00:00', '2026-03-01 11:00:00'),
(@s_e, @aid2, 'ASSIGNMENT', 'COMPLETED', 15, 15, 11, 100.00, '2026-03-02 14:00:00', '2026-03-02 15:00:00'),
-- aid3 (완료, 1/2명): c 완료, g 미완 → 완료율 50%
(@s_c, @aid3, 'ASSIGNMENT', 'COMPLETED', 15, 15,  6, 100.00, '2026-03-02 10:00:00', '2026-03-02 11:00:00'),
-- aid4 (완료, 5/5명): b,c,e,g,h 모두 완료 → 완료율 100%
(@s_b, @aid4, 'ASSIGNMENT', 'COMPLETED',  5,  5,  4, 100.00, '2026-03-04 09:00:00', '2026-03-04 09:30:00'),
(@s_c, @aid4, 'ASSIGNMENT', 'COMPLETED',  5,  5,  2, 100.00, '2026-03-04 10:00:00', '2026-03-04 10:35:00'),
(@s_e, @aid4, 'ASSIGNMENT', 'COMPLETED',  5,  5,  4, 100.00, '2026-03-05 11:00:00', '2026-03-05 11:30:00'),
(@s_g, @aid4, 'ASSIGNMENT', 'COMPLETED',  5,  5,  3, 100.00, '2026-03-05 14:00:00', '2026-03-05 14:40:00'),
(@s_h, @aid4, 'ASSIGNMENT', 'COMPLETED',  5,  5,  4, 100.00, '2026-03-06 09:00:00', '2026-03-06 09:30:00'),
-- aid5 (완료, 3/4명): a,d,f 완료, b 미완 → 완료율 75%
(@s_a, @aid5, 'ASSIGNMENT', 'COMPLETED',  5,  5,  5, 100.00, '2026-03-03 09:00:00', '2026-03-03 09:25:00'),
(@s_d, @aid5, 'ASSIGNMENT', 'COMPLETED',  5,  5,  4, 100.00, '2026-03-04 13:00:00', '2026-03-04 13:30:00'),
(@s_f, @aid5, 'ASSIGNMENT', 'COMPLETED',  5,  5,  3, 100.00, '2026-03-05 15:00:00', '2026-03-05 15:30:00'),
-- aid6 (진행중, 1/3명): a만 완료 → 완료율 33%
(@s_a, @aid6, 'ASSIGNMENT', 'COMPLETED', 15, 15, 13, 100.00, '2026-03-16 09:00:00', '2026-03-16 10:00:00'),
-- aid7 (진행중, 2/4명): b,e 완료 → 완료율 50%
(@s_b, @aid7, 'ASSIGNMENT', 'COMPLETED', 15, 12,  9,  80.00, '2026-03-16 10:00:00', '2026-03-16 11:00:00'),
(@s_e, @aid7, 'ASSIGNMENT', 'COMPLETED', 15, 15, 11, 100.00, '2026-03-17 14:00:00', '2026-03-17 15:00:00'),
-- aid8 (진행중, 2/6명): a,b만 완료 → 완료율 33%
(@s_a, @aid8, 'ASSIGNMENT', 'COMPLETED',  9,  9,  7, 100.00, '2026-03-17 09:00:00', '2026-03-17 09:45:00'),
(@s_b, @aid8, 'ASSIGNMENT', 'COMPLETED',  9,  9,  5, 100.00, '2026-03-17 11:00:00', '2026-03-17 11:50:00'),
-- aid9 (진행중, 0/3명): 아무도 안함 → 완료율 0%
-- 자율 학습
(@s_a, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-13 10:00:00', '2026-03-13 10:30:00'),
(@s_b, NULL, 'SELF', 'COMPLETED', 5, 5, 2, 100.00, '2026-03-13 11:00:00', '2026-03-13 11:30:00');

-- ============================================================
-- 문제 풀이 기록 (LAST_INSERT_ID 방식 — @ses_* 변수 제거)
-- 각 INSERT INTO learning_sessions 직후 LAST_INSERT_ID()로 session_id 확보
-- 세션별 오답 과목: 영어A·수학B·국어C+과학C·사회B+수학B·영어A+국어A
-- ============================================================

-- 세션 PA1: student_a, A레벨 — 영어 전 오답 → 영어 단원 취약
INSERT INTO learning_sessions (student_id, assignment_id, session_type, status, total_problems, solved_count, correct_count, completion_rate, started_at, completed_at)
VALUES (@s_a, @aid1, 'ASSIGNMENT', 'COMPLETED', 17, 17, 14, 100.00, '2026-03-12 10:00:00', '2026-03-12 11:00:00');
INSERT INTO problem_attempts (session_id, student_id, problem_id, submitted_answer, is_correct, time_spent_sec, is_bookmarked, requested_video, attempted_at)
SELECT LAST_INSERT_ID(), @s_a, p.problem_id,
       IF(p.subject_code_id = @subj_eng, '3', '1'),
       IF(p.subject_code_id = @subj_eng, 0, 1),
       90, 0, 0, '2026-03-12 10:00:00'
FROM problems p WHERE p.level='A' ORDER BY p.problem_id;

-- 세션 PA2: student_d, B레벨 — 수학 전 오답 → 수학 단원 취약
INSERT INTO learning_sessions (student_id, assignment_id, session_type, status, total_problems, solved_count, correct_count, completion_rate, started_at, completed_at)
VALUES (@s_d, @aid1, 'ASSIGNMENT', 'COMPLETED', 17, 17, 12, 100.00, '2026-03-12 11:00:00', '2026-03-12 12:00:00');
INSERT INTO problem_attempts (session_id, student_id, problem_id, submitted_answer, is_correct, time_spent_sec, is_bookmarked, requested_video, attempted_at)
SELECT LAST_INSERT_ID(), @s_d, p.problem_id,
       IF(p.subject_code_id = @subj_math, '3', '1'),
       IF(p.subject_code_id = @subj_math, 0, 1),
       90, 0, 0, '2026-03-12 11:00:00'
FROM problems p WHERE p.level='B' ORDER BY p.problem_id;

-- 세션 PA3: student_b, C레벨 — 국어+과학 전 오답 → 국어·과학 단원 취약
INSERT INTO learning_sessions (student_id, assignment_id, session_type, status, total_problems, solved_count, correct_count, completion_rate, started_at, completed_at)
VALUES (@s_b, @aid2, 'ASSIGNMENT', 'COMPLETED', 17, 17, 11, 100.00, '2026-03-12 13:00:00', '2026-03-12 14:00:00');
INSERT INTO problem_attempts (session_id, student_id, problem_id, submitted_answer, is_correct, time_spent_sec, is_bookmarked, requested_video, attempted_at)
SELECT LAST_INSERT_ID(), @s_b, p.problem_id,
       IF(p.subject_code_id IN (@subj_kor, @subj_sci), '3', '1'),
       IF(p.subject_code_id IN (@subj_kor, @subj_sci), 0, 1),
       90, 0, 0, '2026-03-12 13:00:00'
FROM problems p WHERE p.level='C' ORDER BY p.problem_id;

-- 세션 PA4: student_c, B레벨 — 사회+수학 전 오답 → 사회·수학 단원 취약
INSERT INTO learning_sessions (student_id, assignment_id, session_type, status, total_problems, solved_count, correct_count, completion_rate, started_at, completed_at)
VALUES (@s_c, @aid3, 'ASSIGNMENT', 'COMPLETED', 17, 17, 9, 100.00, '2026-03-13 10:00:00', '2026-03-13 11:00:00');
INSERT INTO problem_attempts (session_id, student_id, problem_id, submitted_answer, is_correct, time_spent_sec, is_bookmarked, requested_video, attempted_at)
SELECT LAST_INSERT_ID(), @s_c, p.problem_id,
       IF(p.subject_code_id IN (@subj_soc, @subj_math), '3', '1'),
       IF(p.subject_code_id IN (@subj_soc, @subj_math), 0, 1),
       90, 0, 0, '2026-03-13 10:00:00'
FROM problems p WHERE p.level='B' ORDER BY p.problem_id;

-- 세션 PA5: student_a 자율, A레벨 — 영어+국어 전 오답 → 영어·국어 단원 취약 심화
INSERT INTO learning_sessions (student_id, assignment_id, session_type, status, total_problems, solved_count, correct_count, completion_rate, started_at, completed_at)
VALUES (@s_a, NULL, 'SELF', 'COMPLETED', 17, 17, 11, 100.00, '2026-03-13 11:00:00', '2026-03-13 12:00:00');
INSERT INTO problem_attempts (session_id, student_id, problem_id, submitted_answer, is_correct, time_spent_sec, is_bookmarked, requested_video, attempted_at)
SELECT LAST_INSERT_ID(), @s_a, p.problem_id,
       IF(p.subject_code_id IN (@subj_eng, @subj_kor), '3', '1'),
       IF(p.subject_code_id IN (@subj_eng, @subj_kor), 0, 1),
       90, 0, 0, '2026-03-13 11:00:00'
FROM problems p WHERE p.level='A' ORDER BY p.problem_id;

-- 세션 PA6: student_e, A레벨, 전체 정답
-- → 영어A: 0+0+1=1/3=33%, 국어A: 1+0+1=2/3=67% (0%에서 탈출)
INSERT INTO learning_sessions (student_id, assignment_id, session_type, status, total_problems, solved_count, correct_count, completion_rate, started_at, completed_at)
VALUES (@s_e, NULL, 'SELF', 'COMPLETED', 17, 17, 17, 100.00, '2026-03-14 09:00:00', '2026-03-14 10:00:00');
INSERT INTO problem_attempts (session_id, student_id, problem_id, submitted_answer, is_correct, time_spent_sec, is_bookmarked, requested_video, attempted_at)
SELECT LAST_INSERT_ID(), @s_e, p.problem_id, '1', 1, 90, 0, 0, '2026-03-14 09:00:00'
FROM problems p WHERE p.level='A' ORDER BY p.problem_id;

-- 세션 PA7: student_g, B레벨, 전체 정답
-- → 수학B: 0+0+1=1/3=33%, 사회B: 1+0+1=2/3=67% (0%에서 탈출)
INSERT INTO learning_sessions (student_id, assignment_id, session_type, status, total_problems, solved_count, correct_count, completion_rate, started_at, completed_at)
VALUES (@s_g, NULL, 'SELF', 'COMPLETED', 17, 17, 17, 100.00, '2026-03-14 10:00:00', '2026-03-14 11:00:00');
INSERT INTO problem_attempts (session_id, student_id, problem_id, submitted_answer, is_correct, time_spent_sec, is_bookmarked, requested_video, attempted_at)
SELECT LAST_INSERT_ID(), @s_g, p.problem_id, '1', 1, 90, 0, 0, '2026-03-14 10:00:00'
FROM problems p WHERE p.level='B' ORDER BY p.problem_id;

-- 세션 PA8: student_h, C레벨, 전체 정답
-- → 국어C: 0+1=1/2=50%, 과학C: 0+1=1/2=50% (0%에서 탈출)
INSERT INTO learning_sessions (student_id, assignment_id, session_type, status, total_problems, solved_count, correct_count, completion_rate, started_at, completed_at)
VALUES (@s_h, NULL, 'SELF', 'COMPLETED', 17, 17, 17, 100.00, '2026-03-14 11:00:00', '2026-03-14 12:00:00');
INSERT INTO problem_attempts (session_id, student_id, problem_id, submitted_answer, is_correct, time_spent_sec, is_bookmarked, requested_video, attempted_at)
SELECT LAST_INSERT_ID(), @s_h, p.problem_id, '1', 1, 90, 0, 0, '2026-03-14 11:00:00'
FROM problems p WHERE p.level='C' ORDER BY p.problem_id;

-- ============================================================
-- 학습 리포트 (주간, 4주치)
-- ============================================================
INSERT INTO learning_reports (student_id, report_type, report_date, total_problems, correct_count, score_rate, level_a_count, level_b_count, level_c_count, study_time_sec, weak_units, ai_comment)
SELECT s.student_id, 'WEEKLY',
       CASE w.w WHEN 0 THEN '2026-03-15' WHEN 1 THEN '2026-03-08' WHEN 2 THEN '2026-03-01' WHEN 3 THEN '2026-02-22' END,
       45 - w.w*3, 38 - w.w*2,
       ROUND((38 - w.w*2) / (45 - w.w*3) * 100, 2),
       (45 - w.w*3) DIV 3, (45 - w.w*3) DIV 3, (45 - w.w*3) - ((45 - w.w*3) DIV 3) * 2,
       3000 - w.w*300,
       '["이차방정식"]',
       'A레벨 최상위권으로 꾸준히 성장하고 있습니다. 이차방정식 단원을 집중 보완하면 완벽합니다.'
FROM students s
JOIN users u ON s.user_id = u.user_id
CROSS JOIN (SELECT 0 w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) w
WHERE u.email = 'student_a@test.com';

INSERT INTO learning_reports (student_id, report_type, report_date, total_problems, correct_count, score_rate, level_a_count, level_b_count, level_c_count, study_time_sec, weak_units, ai_comment)
SELECT s.student_id, 'WEEKLY',
       CASE w.w WHEN 0 THEN '2026-03-15' WHEN 1 THEN '2026-03-08' WHEN 2 THEN '2026-03-01' WHEN 3 THEN '2026-02-22' END,
       30 - w.w*3, 18 - w.w*2,
       ROUND((18 - w.w*2) / (30 - w.w*3) * 100, 2),
       (30 - w.w*3) DIV 3, (30 - w.w*3) DIV 3, (30 - w.w*3) - ((30 - w.w*3) DIV 3) * 2,
       2100 - w.w*300,
       '["일차함수","연립방정식"]',
       'B레벨에서 꾸준히 풀고 있습니다. 일차함수와 연립방정식 복습을 권장합니다.'
FROM students s
JOIN users u ON s.user_id = u.user_id
CROSS JOIN (SELECT 0 w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) w
WHERE u.email = 'student_b@test.com';

INSERT INTO learning_reports (student_id, report_type, report_date, total_problems, correct_count, score_rate, level_a_count, level_b_count, level_c_count, study_time_sec, weak_units, ai_comment)
SELECT s.student_id, 'WEEKLY',
       CASE w.w WHEN 0 THEN '2026-03-15' WHEN 1 THEN '2026-03-08' WHEN 2 THEN '2026-03-01' WHEN 3 THEN '2026-02-22' END,
       20 - w.w*3, 8 - w.w*2,
       ROUND((8 - w.w*2) / (20 - w.w*3) * 100, 2),
       (20 - w.w*3) DIV 3, (20 - w.w*3) DIV 3, (20 - w.w*3) - ((20 - w.w*3) DIV 3) * 2,
       1400 - w.w*300,
       '["정수와 유리수","소인수분해"]',
       'C레벨 기초 개념 보완이 필요합니다. 정수와 유리수부터 차근차근 학습해보세요.'
FROM students s
JOIN users u ON s.user_id = u.user_id
CROSS JOIN (SELECT 0 w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) w
WHERE u.email = 'student_c@test.com';

-- ============================================================
-- 공지사항
-- ============================================================
INSERT INTO announcements (school_id, author_id, title, content, is_important, target_role, view_count, is_active, created_at) VALUES
(NULL, (SELECT user_id FROM users WHERE email='admin@edu-platform.com'), '2026년 1분기 서비스 업데이트 안내', '<p>안녕하세요, AI EDU입니다.<br/>2026년 1분기 서비스 업데이트 내용을 안내드립니다.</p><ul><li>AI 진단 테스트 정확도 향상</li><li>UI/UX 개선</li><li>서버 성능 최적화</li></ul>', 1, 'ALL', 342, 1, '2026-03-10 09:00:00'),
(NULL, (SELECT user_id FROM users WHERE email='admin@edu-platform.com'), '교사 대상 기능 추가 안내 - 과제 자동 배정', '<p>교사 전용 기능이 추가되었습니다. 레벨별 과제 자동 배정 기능을 이용해 보세요.</p><p>학생의 진단 결과에 따라 적합한 문제가 자동으로 배정됩니다.</p>', 0, 'TEACHER', 128, 1, '2026-03-08 09:00:00'),
(NULL, (SELECT user_id FROM users WHERE email='admin@edu-platform.com'), '학생 학습 리포트 기능 오픈', '<p>학생 전용 주간 학습 리포트 기능이 오픈되었습니다.</p><p>매주 일요일 자동으로 생성되며 취약 단원 분석과 AI 코멘트가 포함됩니다.</p>', 0, 'STUDENT', 215, 1, '2026-03-05 09:00:00'),
(NULL, (SELECT user_id FROM users WHERE email='admin@edu-platform.com'), '서비스 점검 안내 (3/20 02:00~04:00)', '<p>정기 서비스 점검이 예정되어 있습니다.</p><p>2026년 3월 20일 새벽 2시부터 4시까지 서비스 이용이 불가합니다. 양해 부탁드립니다.</p>', 1, 'ALL', 89, 1, '2026-03-01 09:00:00'),
(NULL, (SELECT user_id FROM users WHERE email='admin@edu-platform.com'), '개인정보처리방침 개정 안내', '<p>개인정보처리방침이 2026년 4월 1일부터 개정됩니다.</p><p>변경된 내용을 반드시 확인해 주시기 바랍니다.</p>', 0, 'ALL', 67, 1, '2026-02-20 09:00:00'),
(NULL, (SELECT user_id FROM users WHERE email='admin@edu-platform.com'), '과학/사회 과목 문제 추가 안내', '<p>과학 및 사회 과목 문제가 새롭게 추가되었습니다.</p><p>A, B, C 레벨 각각 9문제씩 총 18문제가 추가되어 다양한 과목을 학습하실 수 있습니다.</p>', 0, 'ALL', 44, 1, '2026-02-15 09:00:00');

-- ============================================================
-- 추가 학습 세션 (최근 14일 DAU 차트용)
-- ============================================================
SET @sid_a = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_a@test.com');
SET @sid_b = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_b@test.com');
SET @sid_c = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_c@test.com');
SET @sid_d = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_d@test.com');
SET @sid_e = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_e@test.com');
SET @sid_f = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_f@test.com');
SET @sid_g = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_g@test.com');
SET @sid_h = (SELECT student_id FROM students s JOIN users u ON s.user_id=u.user_id WHERE u.email='student_h@test.com');

INSERT INTO learning_sessions (student_id, assignment_id, session_type, status, total_problems, solved_count, correct_count, completion_rate, started_at, completed_at) VALUES
-- 3/2
(@sid_a, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-02 09:10:00', '2026-03-02 09:40:00'),
(@sid_b, NULL, 'SELF', 'COMPLETED', 5, 5, 3, 100.00, '2026-03-02 10:00:00', '2026-03-02 10:30:00'),
-- 3/3
(@sid_c, NULL, 'SELF', 'COMPLETED', 5, 5, 2, 100.00, '2026-03-03 11:00:00', '2026-03-03 11:35:00'),
(@sid_d, NULL, 'SELF', 'COMPLETED', 5, 5, 5, 100.00, '2026-03-03 14:00:00', '2026-03-03 14:25:00'),
-- 3/4
(@sid_a, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-04 09:00:00', '2026-03-04 09:30:00'),
(@sid_e, NULL, 'SELF', 'COMPLETED', 5, 5, 3, 100.00, '2026-03-04 15:00:00', '2026-03-04 15:30:00'),
(@sid_f, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-04 16:00:00', '2026-03-04 16:30:00'),
-- 3/5
(@sid_b, NULL, 'SELF', 'COMPLETED', 5, 5, 2, 100.00, '2026-03-05 10:00:00', '2026-03-05 10:40:00'),
(@sid_g, NULL, 'SELF', 'COMPLETED', 5, 5, 1, 100.00, '2026-03-05 11:00:00', '2026-03-05 11:45:00'),
-- 3/6
(@sid_a, NULL, 'SELF', 'COMPLETED', 5, 5, 5, 100.00, '2026-03-06 08:30:00', '2026-03-06 09:00:00'),
(@sid_d, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-06 13:00:00', '2026-03-06 13:30:00'),
(@sid_h, NULL, 'SELF', 'COMPLETED', 5, 5, 3, 100.00, '2026-03-06 15:00:00', '2026-03-06 15:30:00'),
-- 3/7
(@sid_c, NULL, 'SELF', 'COMPLETED', 5, 5, 3, 100.00, '2026-03-07 10:00:00', '2026-03-07 10:35:00'),
(@sid_e, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-07 14:30:00', '2026-03-07 15:00:00'),
-- 3/8
(@sid_a, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-08 09:00:00', '2026-03-08 09:30:00'),
(@sid_b, NULL, 'SELF', 'COMPLETED', 5, 5, 3, 100.00, '2026-03-08 10:30:00', '2026-03-08 11:00:00'),
(@sid_f, NULL, 'SELF', 'COMPLETED', 5, 5, 5, 100.00, '2026-03-08 13:00:00', '2026-03-08 13:25:00'),
(@sid_g, NULL, 'SELF', 'COMPLETED', 5, 5, 2, 100.00, '2026-03-08 14:00:00', '2026-03-08 14:50:00'),
-- 3/9
(@sid_d, NULL, 'SELF', 'COMPLETED', 5, 5, 5, 100.00, '2026-03-09 09:00:00', '2026-03-09 09:25:00'),
(@sid_h, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-09 11:00:00', '2026-03-09 11:30:00'),
-- 3/10
(@sid_a, NULL, 'SELF', 'COMPLETED', 5, 5, 5, 100.00, '2026-03-10 08:00:00', '2026-03-10 08:30:00'),
(@sid_b, NULL, 'SELF', 'COMPLETED', 5, 5, 3, 100.00, '2026-03-10 09:00:00', '2026-03-10 09:30:00'),
(@sid_c, NULL, 'SELF', 'COMPLETED', 5, 5, 2, 100.00, '2026-03-10 10:00:00', '2026-03-10 10:40:00'),
(@sid_e, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-10 14:00:00', '2026-03-10 14:30:00'),
-- 3/11
(@sid_a, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-11 09:10:00', '2026-03-11 09:40:00'),
(@sid_d, NULL, 'SELF', 'COMPLETED', 5, 5, 5, 100.00, '2026-03-11 10:00:00', '2026-03-11 10:25:00'),
(@sid_f, NULL, 'SELF', 'COMPLETED', 5, 5, 3, 100.00, '2026-03-11 13:00:00', '2026-03-11 13:35:00'),
(@sid_g, NULL, 'SELF', 'COMPLETED', 5, 5, 2, 100.00, '2026-03-11 15:00:00', '2026-03-11 15:50:00'),
(@sid_h, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-11 16:00:00', '2026-03-11 16:30:00'),
-- 3/14
(@sid_a, NULL, 'SELF', 'COMPLETED', 5, 5, 5, 100.00, '2026-03-14 09:00:00', '2026-03-14 09:30:00'),
(@sid_b, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-14 10:00:00', '2026-03-14 10:30:00'),
(@sid_c, NULL, 'SELF', 'COMPLETED', 5, 5, 2, 100.00, '2026-03-14 11:00:00', '2026-03-14 11:40:00'),
(@sid_d, NULL, 'SELF', 'COMPLETED', 5, 5, 5, 100.00, '2026-03-14 14:00:00', '2026-03-14 14:25:00'),
(@sid_e, NULL, 'SELF', 'COMPLETED', 5, 5, 3, 100.00, '2026-03-14 15:00:00', '2026-03-14 15:30:00'),
(@sid_f, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-14 16:00:00', '2026-03-14 16:30:00'),
-- 3/15
(@sid_a, NULL, 'SELF', 'COMPLETED', 5, 5, 5, 100.00, '2026-03-15 09:00:00', '2026-03-15 09:30:00'),
(@sid_b, NULL, 'SELF', 'COMPLETED', 5, 5, 3, 100.00, '2026-03-15 10:00:00', '2026-03-15 10:35:00'),
(@sid_d, NULL, 'SELF', 'COMPLETED', 5, 5, 5, 100.00, '2026-03-15 11:00:00', '2026-03-15 11:25:00'),
(@sid_e, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-15 13:00:00', '2026-03-15 13:30:00'),
(@sid_g, NULL, 'SELF', 'COMPLETED', 5, 5, 3, 100.00, '2026-03-15 14:00:00', '2026-03-15 14:40:00'),
(@sid_h, NULL, 'SELF', 'COMPLETED', 5, 5, 4, 100.00, '2026-03-15 15:00:00', '2026-03-15 15:30:00');

-- ============================================================
-- 구독 플랜
-- ============================================================
INSERT INTO subscription_plans (plan_name, plan_type, price, duration_days, features, is_active) VALUES
('베이직',   'BASIC',    9900,  30,  '{"problems":true,"videos":false,"ai_hint":false,"report":false}', 1),
('스탠다드', 'STANDARD', 19900, 30,  '{"problems":true,"videos":true,"ai_hint":false,"report":true}',  1),
('프리미엄', 'PREMIUM',  29900, 30,  '{"problems":true,"videos":true,"ai_hint":true,"report":true}',   1);

-- ============================================================
-- 구독 정보
-- ============================================================
SET @plan_basic    = (SELECT plan_id FROM subscription_plans WHERE plan_type='BASIC'    LIMIT 1);
SET @plan_standard = (SELECT plan_id FROM subscription_plans WHERE plan_type='STANDARD' LIMIT 1);
SET @plan_premium  = (SELECT plan_id FROM subscription_plans WHERE plan_type='PREMIUM'  LIMIT 1);

SET @uid_a = (SELECT user_id FROM users WHERE email='student_a@test.com');
SET @uid_b = (SELECT user_id FROM users WHERE email='student_b@test.com');
SET @uid_c = (SELECT user_id FROM users WHERE email='student_c@test.com');
SET @uid_d = (SELECT user_id FROM users WHERE email='student_d@test.com');
SET @uid_e = (SELECT user_id FROM users WHERE email='student_e@test.com');
SET @uid_f = (SELECT user_id FROM users WHERE email='student_f@test.com');
SET @uid_g = (SELECT user_id FROM users WHERE email='student_g@test.com');
SET @uid_h = (SELECT user_id FROM users WHERE email='student_h@test.com');
SET @uid_t = (SELECT user_id FROM users WHERE email='teacher@test.com');
SET @uid_t2= (SELECT user_id FROM users WHERE email='teacher2@test.com');

INSERT INTO subscriptions (user_id, school_id, plan_id, status, start_date, end_date, auto_renew) VALUES
(@uid_a, 1, @plan_premium,  'ACTIVE',    '2026-03-01', '2026-03-31', 1),
(@uid_b, 1, @plan_standard, 'ACTIVE',    '2026-03-01', '2026-03-31', 1),
(@uid_c, 1, @plan_basic,    'ACTIVE',    '2026-03-01', '2026-03-31', 0),
(@uid_d, 1, @plan_premium,  'ACTIVE',    '2026-03-05', '2026-04-04', 1),
(@uid_e, 1, @plan_standard, 'ACTIVE',    '2026-03-05', '2026-04-04', 1),
(@uid_f, 1, @plan_basic,    'ACTIVE',    '2026-03-08', '2026-04-07', 1),
(@uid_g, 1, @plan_basic,    'EXPIRED',   '2026-02-01', '2026-02-28', 0),
(@uid_h, 1, @plan_standard, 'ACTIVE',    '2026-03-10', '2026-04-09', 1),
(@uid_t, 1, @plan_premium,  'ACTIVE',    '2026-02-01', '2026-04-30', 1),
(@uid_t2,1, @plan_standard, 'ACTIVE',    '2026-03-01', '2026-03-31', 1),
-- 과거 구독 (이력용)
(@uid_a, 1, @plan_standard, 'EXPIRED',   '2026-02-01', '2026-02-28', 1),
(@uid_b, 1, @plan_basic,    'EXPIRED',   '2026-02-01', '2026-02-28', 1),
(@uid_c, 1, @plan_basic,    'CANCELLED', '2026-01-01', '2026-01-31', 0);

-- ============================================================
-- 결제 이력
-- ============================================================
SET @sub1  = (SELECT subscription_id FROM subscriptions WHERE user_id=@uid_a  AND status='ACTIVE' LIMIT 1);
SET @sub2  = (SELECT subscription_id FROM subscriptions WHERE user_id=@uid_b  AND status='ACTIVE' LIMIT 1);
SET @sub3  = (SELECT subscription_id FROM subscriptions WHERE user_id=@uid_c  AND status='ACTIVE' LIMIT 1);
SET @sub4  = (SELECT subscription_id FROM subscriptions WHERE user_id=@uid_d  AND status='ACTIVE' LIMIT 1);
SET @sub5  = (SELECT subscription_id FROM subscriptions WHERE user_id=@uid_e  AND status='ACTIVE' LIMIT 1);
SET @sub6  = (SELECT subscription_id FROM subscriptions WHERE user_id=@uid_f  AND status='ACTIVE' LIMIT 1);
SET @sub8  = (SELECT subscription_id FROM subscriptions WHERE user_id=@uid_h  AND status='ACTIVE' LIMIT 1);
SET @sub9  = (SELECT subscription_id FROM subscriptions WHERE user_id=@uid_t  AND status='ACTIVE' LIMIT 1);
SET @sub10 = (SELECT subscription_id FROM subscriptions WHERE user_id=@uid_t2 AND status='ACTIVE' LIMIT 1);

INSERT INTO payments (user_id, subscription_id, pg_provider, pg_tid, merchant_uid, amount, status, paid_at) VALUES
(@uid_a, @sub1,  '토스페이먼츠', 'toss_20260301_001', 'ORD-20260301-001', 29900, 'COMPLETED', '2026-03-01 10:05:22'),
(@uid_b, @sub2,  '토스페이먼츠', 'toss_20260301_002', 'ORD-20260301-002', 19900, 'COMPLETED', '2026-03-01 11:12:44'),
(@uid_c, @sub3,  '아임포트',     'imp_20260301_003',  'ORD-20260301-003',  9900, 'COMPLETED', '2026-03-01 14:33:11'),
(@uid_d, @sub4,  '토스페이먼츠', 'toss_20260305_004', 'ORD-20260305-004', 29900, 'COMPLETED', '2026-03-05 09:20:05'),
(@uid_e, @sub5,  '아임포트',     'imp_20260305_005',  'ORD-20260305-005', 19900, 'COMPLETED', '2026-03-05 15:44:30'),
(@uid_f, @sub6,  '토스페이먼츠', 'toss_20260308_006', 'ORD-20260308-006',  9900, 'COMPLETED', '2026-03-08 10:10:55'),
(@uid_h, @sub8,  '아임포트',     'imp_20260310_008',  'ORD-20260310-008', 19900, 'COMPLETED', '2026-03-10 13:22:17'),
(@uid_t, @sub9,  '토스페이먼츠', 'toss_20260201_009', 'ORD-20260201-009', 29900, 'COMPLETED', '2026-02-01 09:00:00'),
(@uid_t2,@sub10, '토스페이먼츠', 'toss_20260301_010', 'ORD-20260301-010', 19900, 'COMPLETED', '2026-03-01 09:30:00'),
-- 과거 결제
(@uid_a, NULL, '토스페이먼츠', 'toss_20260201_011', 'ORD-20260201-011', 19900, 'COMPLETED', '2026-02-01 10:00:00'),
(@uid_b, NULL, '아임포트',     'imp_20260201_012',  'ORD-20260201-012',  9900, 'COMPLETED', '2026-02-01 11:00:00'),
(@uid_c, NULL, '아임포트',     'imp_20260101_013',  'ORD-20260101-013',  9900, 'COMPLETED', '2026-01-01 12:00:00'),
-- 환불
(@uid_g, NULL, '토스페이먼츠', 'toss_20260201_014', 'ORD-20260201-014',  9900, 'REFUNDED',  '2026-02-01 10:00:00');

-- ============================================================
-- 동영상 샘플
-- ============================================================
SET @uploader = (SELECT user_id FROM users WHERE email='admin@edu-platform.com' LIMIT 1);

INSERT INTO videos (problem_id, title, subject, video_type, video_url, thumbnail_url, duration_sec, file_size_mb, school_id, grade, level, unit_code_id, unit_name, view_count, is_active, uploaded_by) VALUES
(NULL, '이차방정식 풀이 - 인수분해',   '수학', 'UPLOAD', '/api/uploads/videos/math-eq2-factoring.mp4',    '/api/uploads/videos/math-eq2-factoring.jpg',    720,  52.80, NULL, 'GRADE_2', 'A', @unit_math_eq2,    '이차방정식',   142, 1, @uploader),
(NULL, '이차방정식 풀이 - 근의 공식',  '수학', 'UPLOAD', '/api/uploads/videos/math-eq2-formula.mp4',     '/api/uploads/videos/math-eq2-formula.jpg',     540,  52.80, NULL, 'GRADE_2', 'B', @unit_math_eq2,    '이차방정식',    98, 1, @uploader),
(NULL, '삼각함수 기본 개념',           '수학', 'UPLOAD', '/api/uploads/videos/math-trig-basic.mp4',      '/api/uploads/videos/math-trig-basic.jpg',      660,  52.80, NULL, 'GRADE_3', 'A', @unit_math_trig,   '삼각함수',      76, 1, @uploader),
(NULL, '수열의 합 - 등차수열',         '수학', 'UPLOAD', '/api/uploads/videos/math-seq-sum.mp4',         '/api/uploads/videos/math-seq-sum.jpg',         480,  52.80, NULL, 'GRADE_2', 'B', @unit_math_seq,    '수열',          55, 1, @uploader),
(NULL, '영어 독해 - 주제 파악',        '영어', 'UPLOAD', '/api/uploads/videos/eng-reading-topic.mp4',    '/api/uploads/videos/eng-reading-topic.jpg',    600,  52.80, NULL, 'GRADE_2', 'B', @unit_eng_topic,   '독해',          44, 1, @uploader),
(NULL, '영어 문법 - 관계대명사',       '영어', 'UPLOAD', '/api/uploads/videos/eng-grammar-relative.mp4', '/api/uploads/videos/eng-grammar-relative.jpg', 480,  52.80, NULL, 'GRADE_1', 'A', @unit_eng_rel,     '관계대명사',    33, 1, @uploader),
(NULL, '국어 현대시 분석',             '국어', 'UPLOAD', '/api/uploads/videos/kor-modern-poem.mp4',      '/api/uploads/videos/kor-modern-poem.jpg',      540,  52.80, NULL, 'GRADE_1', 'B', @unit_kor_poem,    '현대시',        28, 1, @uploader),
(NULL, '소인수분해 기초',              '수학', 'UPLOAD', '/api/uploads/videos/math-factor-basic.mp4',    '/api/uploads/videos/math-factor-basic.jpg',    360,  52.80, NULL, 'GRADE_1', 'C', @unit_math_factor, '소인수분해',    87, 1, @uploader);

-- 동영상 시청 이력
INSERT INTO video_views (video_id, student_id, watch_time_sec, is_completed, viewed_at)
SELECT v.video_id, @s_a, v.duration_sec, 1, '2026-03-10 10:00:00'
FROM videos v WHERE v.title = '이차방정식 풀이 - 인수분해';
INSERT INTO video_views (video_id, student_id, watch_time_sec, is_completed, viewed_at)
SELECT v.video_id, @s_a, 300, 0, '2026-03-11 09:00:00'
FROM videos v WHERE v.title = '삼각함수 기본 개념';
INSERT INTO video_views (video_id, student_id, watch_time_sec, is_completed, viewed_at)
SELECT v.video_id, @s_b, v.duration_sec, 1, '2026-03-12 14:00:00'
FROM videos v WHERE v.title = '소인수분해 기초';
INSERT INTO video_views (video_id, student_id, watch_time_sec, is_completed, viewed_at)
SELECT v.video_id, @s_b, v.duration_sec, 1, '2026-03-13 10:00:00'
FROM videos v WHERE v.title = '영어 독해 - 주제 파악';
INSERT INTO video_views (video_id, student_id, watch_time_sec, is_completed, viewed_at)
SELECT v.video_id, @s_c, 180, 0, '2026-03-14 11:00:00'
FROM videos v WHERE v.title = '국어 현대시 분석';

-- ============================================================
-- 문의 샘플
-- ============================================================
INSERT INTO inquiries (user_id, title, content, category, status, created_at) VALUES
(@uid_a, '진단 테스트 결과가 이상해요', '진단 테스트를 완료했는데 결과 페이지가 안 뜹니다.', 'ETC',      'COMPLETED',    '2026-03-10 10:00:00'),
(@uid_b, '동영상이 재생이 안 돼요',     '동영상 풀이 클릭하면 화면이 흰색으로만 나옵니다.',  'ETC',      'PENDING',      '2026-03-12 14:00:00'),
(@uid_c, '레벨 조정 요청드립니다',      '현재 C레벨인데 B레벨로 올려주실 수 있나요?',         'LEARNING', 'COMPLETED',    '2026-03-08 09:00:00'),
(@uid_t, '과제 자동 배정 기능 문의',    '레벨별 자동 배정 기준이 어떻게 되는지 궁금합니다.',  'LEARNING', 'PENDING',      '2026-03-14 11:00:00'),
(@uid_d, '결제 영수증 재발행 요청',     '3월 1일 결제 영수증을 재발행 받고 싶습니다.',         'PAYMENT',  'COMPLETED',    '2026-03-11 16:00:00');

SET @admin_id = (SELECT user_id FROM users WHERE email='admin@edu-platform.com' LIMIT 1);

INSERT INTO inquiry_replies (inquiry_id, replier_id, content, created_at) VALUES
(1, @admin_id, '안녕하세요. 확인 결과 브라우저 캐시 문제로 확인됩니다. 캐시 삭제 후 재시도해 주세요.', '2026-03-10 15:00:00'),
(3, @admin_id, '안녕하세요. 레벨 조정을 위해서는 추가 진단 테스트가 필요합니다. 진단 페이지에서 재진단을 진행해 주세요.', '2026-03-09 10:00:00'),
(5, @admin_id, '안녕하세요. 영수증 재발행 처리해 드렸습니다. 등록된 이메일로 발송되었습니다.', '2026-03-12 09:00:00');

-- ============================================================
-- 교사 → 학생 피드백 (과제 전체 피드백)
-- ============================================================
SET @teacher1 = (SELECT teacher_id FROM teachers t JOIN users u ON t.user_id=u.user_id WHERE u.email='teacher@test.com');

-- aid1 (3월 A레벨 과제): 학생 a,d,f 완료 → 교사가 a,d에게 전체 피드백 작성
INSERT INTO assignment_feedbacks (assignment_id, student_id, teacher_id, comment, created_at, updated_at) VALUES
(@aid1, @s_a, @teacher1, '영어 파트에서 오답이 집중되고 있습니다. 독해 지문을 꼼꼼히 읽는 연습이 필요합니다. 문맥 속 대명사가 가리키는 대상을 정확히 파악하는 훈련을 해보세요.', '2026-03-13 09:00:00', '2026-03-13 09:00:00'),
(@aid1, @s_d, @teacher1, '수학 전반적으로 개념 이해가 부족합니다. 특히 이차방정식과 함수 파트를 교과서 예제부터 다시 풀어보는 것을 권장합니다.', '2026-03-13 10:00:00', '2026-03-13 10:00:00');

-- aid2 (3월 B레벨 과제): 학생 b 완료 → 교사가 b에게 전체 피드백 작성
INSERT INTO assignment_feedbacks (assignment_id, student_id, teacher_id, comment, created_at, updated_at) VALUES
(@aid2, @s_b, @teacher1, '국어와 과학 파트에서 오답률이 높습니다. 국어는 지문 분석 능력을, 과학은 기본 개념 암기를 강화해주세요. 전반적으로 성실하게 풀이한 점은 좋습니다.', '2026-03-13 11:00:00', '2026-03-13 11:00:00');

-- aid4 (수학 1단원 복습): 학생 c에게 전체 피드백
INSERT INTO assignment_feedbacks (assignment_id, student_id, teacher_id, comment, created_at, updated_at) VALUES
(@aid4, @s_c, @teacher1, '사회와 수학에서 오답이 많습니다. 사회는 핵심 개념 정리 노트를 만들어보고, 수학은 기본 연산부터 차근차근 복습하세요.', '2026-03-14 09:00:00', '2026-03-14 09:00:00');

-- ============================================================
-- 교사 → 학생 피드백 (문제별 오답 피드백)
-- 세션 PA1: student_a, 영어 오답 3문제에 대한 피드백
-- ============================================================
INSERT INTO attempt_feedbacks (attempt_id, teacher_id, comment, created_at, updated_at)
SELECT pa.attempt_id, @teacher1,
       CASE
         WHEN p.question_text LIKE '%밑줄 친 "it"%' THEN '대명사 "it"이 가리키는 대상은 앞 문장의 주어입니다. 지문에서 주어-동사 관계를 먼저 파악한 후 대명사를 해석해보세요.'
         WHEN p.question_text LIKE '%has / she / studied%' THEN '영어 어순은 주어+동사+목적어가 기본입니다. 현재완료 시제에서는 주어+has/have+과거분사 순서를 기억하세요.'
         WHEN p.question_text LIKE '%환경 보호 관련 지문%' THEN '글의 요지를 파악할 때는 첫 문장과 마지막 문장에 주목하세요. 필자가 반복적으로 강조하는 키워드를 찾아보세요.'
       END,
       '2026-03-13 09:30:00', '2026-03-13 09:30:00'
FROM problem_attempts pa
JOIN problems p ON pa.problem_id = p.problem_id
JOIN learning_sessions ls ON pa.session_id = ls.session_id
WHERE pa.student_id = @s_a AND pa.is_correct = 0
  AND p.subject_code_id = @subj_eng AND p.level = 'A'
  AND ls.assignment_id = @aid1
LIMIT 3;

-- 세션 PA2: student_d, 수학 오답 5문제 중 3개에 피드백
INSERT INTO attempt_feedbacks (attempt_id, teacher_id, comment, created_at, updated_at)
SELECT pa.attempt_id, @teacher1,
       CASE
         WHEN p.question_text LIKE '%y=2x+3%' THEN 'x절편은 y=0일 때의 x값입니다. 0=2x+3을 풀면 x=-3/2가 됩니다. 절편의 정의를 다시 확인해보세요.'
         WHEN p.question_text LIKE '%x+y=5%' THEN '연립방정식은 가감법 또는 대입법으로 풀 수 있습니다. 두 식을 더하면 y가 소거되어 x를 구할 수 있습니다.'
         WHEN p.question_text LIKE '%밑변 6, 높이 4%' THEN '삼각형 넓이 = 1/2 × 밑변 × 높이 공식을 정확히 적용하세요. 1/2 곱하는 것을 잊지 마세요.'
       END,
       '2026-03-13 10:30:00', '2026-03-13 10:30:00'
FROM problem_attempts pa
JOIN problems p ON pa.problem_id = p.problem_id
JOIN learning_sessions ls ON pa.session_id = ls.session_id
WHERE pa.student_id = @s_d AND pa.is_correct = 0
  AND p.subject_code_id = @subj_math AND p.level = 'B'
  AND ls.assignment_id = @aid1
  AND (p.question_text LIKE '%y=2x+3%' OR p.question_text LIKE '%x+y=5%' OR p.question_text LIKE '%밑변 6, 높이 4%');

-- 세션 PA3: student_b, 국어 오답에 대한 피드백
INSERT INTO attempt_feedbacks (attempt_id, teacher_id, comment, created_at, updated_at)
SELECT pa.attempt_id, @teacher1,
       CASE
         WHEN p.question_text LIKE '%명사가 아닌 것%' THEN '"아름다운"은 형용사입니다. 명사는 사물의 이름을 나타내고, 형용사는 상태나 성질을 나타냅니다. 품사 구분을 연습해보세요.'
         WHEN p.question_text LIKE '%시조에서 초장%' THEN '시조의 초장은 보통 시상의 시작으로, 자연 소재가 많이 등장합니다. 작품의 시대적 배경과 함께 이해하면 좋습니다.'
         WHEN p.question_text LIKE '%강연의 중심 내용%' THEN '강연의 중심 내용은 반복되는 핵심어와 결론 부분에서 찾을 수 있습니다. 전체 흐름을 파악해보세요.'
       END,
       '2026-03-13 11:30:00', '2026-03-13 11:30:00'
FROM problem_attempts pa
JOIN problems p ON pa.problem_id = p.problem_id
JOIN learning_sessions ls ON pa.session_id = ls.session_id
WHERE pa.student_id = @s_b AND pa.is_correct = 0
  AND p.subject_code_id = @subj_kor AND p.level = 'C'
  AND ls.assignment_id = @aid2
LIMIT 3;

-- ============================================================
-- 북마크 (즐겨찾기) 더미
-- ============================================================
INSERT INTO bookmarks (student_id, problem_id, created_at)
SELECT @s_a, p.problem_id, '2026-03-12 10:30:00'
FROM problems p WHERE p.question_text LIKE '%sin30%' LIMIT 1;
INSERT INTO bookmarks (student_id, problem_id, created_at)
SELECT @s_a, p.problem_id, '2026-03-12 10:31:00'
FROM problems p WHERE p.question_text LIKE '%log₂8%' LIMIT 1;
INSERT INTO bookmarks (student_id, problem_id, created_at)
SELECT @s_a, p.problem_id, '2026-03-13 11:00:00'
FROM problems p WHERE p.question_text LIKE '%밑줄 친 "it"%' LIMIT 1;
INSERT INTO bookmarks (student_id, problem_id, created_at)
SELECT @s_b, p.problem_id, '2026-03-13 14:00:00'
FROM problems p WHERE p.question_text LIKE '%(-3)+(-5)%' LIMIT 1;
INSERT INTO bookmarks (student_id, problem_id, created_at)
SELECT @s_b, p.problem_id, '2026-03-14 09:00:00'
FROM problems p WHERE p.question_text LIKE '%액체가 기체%' LIMIT 1;
