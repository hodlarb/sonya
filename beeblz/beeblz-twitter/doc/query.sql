/**
 * 1. Popular User에 대한 tweet, replied, retweeted, mentioned 수 및 LIWC 합계 
 */
SELECT 
 id1, 
 COUNT(DISTINCT id2), 
 SUM(replyed_count_by_user2), 
 SUM(retweeted_count_by_user2), 
 SUM(mentioned_count_by_user2), 
 SUM(positive_attitude_count_by_user2), 
 SUM(negative_attitude_count_by_user2), 
 SUM(liwc_wc),
 SUM(liwc_wps),
 SUM(liwc_dic),
 SUM(liwc_sixltr),
 SUM(liwc_pronoun),
 SUM(liwc_i),
 SUM(liwc_we),
 SUM(liwc_self),
 SUM(liwc_you),
 SUM(liwc_other),
 SUM(liwc_negate),
 SUM(liwc_assent),
 SUM(liwc_article),
 SUM(liwc_preps),
 SUM(liwc_number),
 SUM(liwc_affect),
 SUM(liwc_posemo),
 SUM(liwc_posfeel),
 SUM(liwc_optim),
 SUM(liwc_negemo),
 SUM(liwc_anx),
 SUM(liwc_anger),
 SUM(liwc_sad),
 SUM(liwc_cogmech),
 SUM(liwc_cause),
 SUM(liwc_insight),
 SUM(liwc_discrep),
 SUM(liwc_inhib),
 SUM(liwc_tentat),
 SUM(liwc_certain),
 SUM(liwc_senses),
 SUM(liwc_see),
 SUM(liwc_hear),
 SUM(liwc_feel),
 SUM(liwc_social),
 SUM(liwc_comm),
 SUM(liwc_othref),
 SUM(liwc_friends),
 SUM(liwc_family),
 SUM(liwc_humans),
 SUM(liwc_time),
 SUM(liwc_past),
 SUM(liwc_present),
 SUM(liwc_future),
 SUM(liwc_space),
 SUM(liwc_up),
 SUM(liwc_down),
 SUM(liwc_incl),
 SUM(liwc_excl),
 SUM(liwc_motion),
 SUM(liwc_occup),
 SUM(liwc_school),
 SUM(liwc_job),
 SUM(liwc_achieve),
 SUM(liwc_leisure),
 SUM(liwc_home),
 SUM(liwc_sports),
 SUM(liwc_tv),
 SUM(liwc_music),
 SUM(liwc_money),
 SUM(liwc_metaph),
 SUM(liwc_relig),
 SUM(liwc_death),
 SUM(liwc_physcal),
 SUM(liwc_body),
 SUM(liwc_sexual),
 SUM(liwc_eating),
 SUM(liwc_sleep),
 SUM(liwc_groom),
 SUM(liwc_swear),
 SUM(liwc_nonfl),
 SUM(liwc_fillers)
FROM relationship 
WHERE id1 IN (SELECT id FROM USER)
GROUP BY id1;


/**
 * 2. Popular User에 대한 tweet, replied, retweeted, mentioned한 유저수 및 LIWC 유저수 
 */
/* 0.tweet한 유저수 */
SELECT id1, COUNT(DISTINCT id2) AS tweet_user_count
FROM relationship
WHERE id1 IN (SELECT id FROM USER)
GROUP BY id1;

/* 1.replied 유저수 */
SELECT id1, COUNT(DISTINCT id2) AS replied_user_count
FROM relationship 
WHERE replyed_count_by_user2 > 0 AND id1 IN (SELECT id FROM USER)
GROUP BY id1;

/* 2.retweeted 유저수 */
SELECT id1, COUNT(DISTINCT id2) AS retweeted_user_count
FROM relationship 
WHERE retweeted_count_by_user2 > 0 AND id1 IN (SELECT id FROM USER)
GROUP BY id1;

/* 3.mentioned 유저수 */
SELECT id1, COUNT(DISTINCT id2) AS mentioned_user_count
FROM relationship 
WHERE mentioned_count_by_user2 > 0 AND id1 IN (SELECT id FROM USER)
GROUP BY id1;

/* 4.liwc postive 유저수 */
SELECT id1, COUNT(DISTINCT id2) AS liwc_posemo_user_count
FROM relationship 
WHERE liwc_posemo > 0 AND id1 IN (SELECT id FROM USER)
GROUP BY id1;

/* 5.liwc negative 유저수 */
SELECT id1, COUNT(DISTINCT id2) AS liwc_negemo_user_count
FROM relationship 
WHERE liwc_negemo > 0 AND id1 IN (SELECT id FROM USER)
GROUP BY id1;

/* 6.liwc postive and negative 유저수 */
SELECT id1, COUNT(DISTINCT id2) AS liwc_posemo_negemo_user_count
FROM relationship 
WHERE liwc_posemo > 0 AND liwc_negemo > 0 AND id1 IN (SELECT id FROM USER)
GROUP BY id1;


/**
 * 3. Popular User가 tweet한 수 및 LIWC 합계 
 */
SELECT USER,
 COUNT(tweet_type),
 COUNT(DISTINCT target_user),  
 SUM(liwc_wc),
 SUM(liwc_wps),
 SUM(liwc_dic),
 SUM(liwc_sixltr),
 SUM(liwc_pronoun),
 SUM(liwc_i),
 SUM(liwc_we),
 SUM(liwc_self),
 SUM(liwc_you),
 SUM(liwc_other),
 SUM(liwc_negate),
 SUM(liwc_assent),
 SUM(liwc_article),
 SUM(liwc_preps),
 SUM(liwc_number),
 SUM(liwc_affect),
 SUM(liwc_posemo),
 SUM(liwc_posfeel),
 SUM(liwc_optim),
 SUM(liwc_negemo),
 SUM(liwc_anx),
 SUM(liwc_anger),
 SUM(liwc_sad),
 SUM(liwc_cogmech),
 SUM(liwc_cause),
 SUM(liwc_insight),
 SUM(liwc_discrep),
 SUM(liwc_inhib),
 SUM(liwc_tentat),
 SUM(liwc_certain),
 SUM(liwc_senses),
 SUM(liwc_see),
 SUM(liwc_hear),
 SUM(liwc_feel),
 SUM(liwc_social),
 SUM(liwc_comm),
 SUM(liwc_othref),
 SUM(liwc_friends),
 SUM(liwc_family),
 SUM(liwc_humans),
 SUM(liwc_time),
 SUM(liwc_past),
 SUM(liwc_present),
 SUM(liwc_future),
 SUM(liwc_space),
 SUM(liwc_up),
 SUM(liwc_down),
 SUM(liwc_incl),
 SUM(liwc_excl),
 SUM(liwc_motion),
 SUM(liwc_occup),
 SUM(liwc_school),
 SUM(liwc_job),
 SUM(liwc_achieve),
 SUM(liwc_leisure),
 SUM(liwc_home),
 SUM(liwc_sports),
 SUM(liwc_tv),
 SUM(liwc_music),
 SUM(liwc_money),
 SUM(liwc_metaph),
 SUM(liwc_relig),
 SUM(liwc_death),
 SUM(liwc_physcal),
 SUM(liwc_body),
 SUM(liwc_sexual),
 SUM(liwc_eating),
 SUM(liwc_sleep),
 SUM(liwc_groom),
 SUM(liwc_swear),
 SUM(liwc_nonfl),
 SUM(liwc_fillers)
FROM tweet 
WHERE USER IN (SELECT id FROM USER)
GROUP BY USER;

/**
 * 4. Popular User별/날짜별/Tweet타입별 tweet 수, tweet 유저수 및 LIWC 합계
 */
SELECT target_user, 
 create_date, 
 tweet_type, 
 COUNT(DISTINCT USER), 
 COUNT(tweet_text),
 SUM(positive_attitude), 
 SUM(negative_attitude), 
 SUM(liwc_wc),
 SUM(liwc_wps),
 SUM(liwc_dic),
 SUM(liwc_sixltr),
 SUM(liwc_pronoun),
 SUM(liwc_i),
 SUM(liwc_we),
 SUM(liwc_self),
 SUM(liwc_you),
 SUM(liwc_other),
 SUM(liwc_negate),
 SUM(liwc_assent),
 SUM(liwc_article),
 SUM(liwc_preps),
 SUM(liwc_number),
 SUM(liwc_affect),
 SUM(liwc_posemo),
 SUM(liwc_posfeel),
 SUM(liwc_optim),
 SUM(liwc_negemo),
 SUM(liwc_anx),
 SUM(liwc_anger),
 SUM(liwc_sad),
 SUM(liwc_cogmech),
 SUM(liwc_cause),
 SUM(liwc_insight),
 SUM(liwc_discrep),
 SUM(liwc_inhib),
 SUM(liwc_tentat),
 SUM(liwc_certain),
 SUM(liwc_senses),
 SUM(liwc_see),
 SUM(liwc_hear),
 SUM(liwc_feel),
 SUM(liwc_social),
 SUM(liwc_comm),
 SUM(liwc_othref),
 SUM(liwc_friends),
 SUM(liwc_family),
 SUM(liwc_humans),
 SUM(liwc_time),
 SUM(liwc_past),
 SUM(liwc_present),
 SUM(liwc_future),
 SUM(liwc_space),
 SUM(liwc_up),
 SUM(liwc_down),
 SUM(liwc_incl),
 SUM(liwc_excl),
 SUM(liwc_motion),
 SUM(liwc_occup),
 SUM(liwc_school),
 SUM(liwc_job),
 SUM(liwc_achieve),
 SUM(liwc_leisure),
 SUM(liwc_home),
 SUM(liwc_sports),
 SUM(liwc_tv),
 SUM(liwc_music),
 SUM(liwc_money),
 SUM(liwc_metaph),
 SUM(liwc_relig),
 SUM(liwc_death),
 SUM(liwc_physcal),
 SUM(liwc_body),
 SUM(liwc_sexual),
 SUM(liwc_eating),
 SUM(liwc_sleep),
 SUM(liwc_groom),
 SUM(liwc_swear),
 SUM(liwc_nonfl),
 SUM(liwc_fillers) 
FROM tweet
WHERE target_user IN (SELECT id FROM USER)
GROUP BY target_user, create_date, tweet_type;


/**
 * 5. Popular User별/날짜별 tweet, replied, retweeted, mentioned 수 및 LIWC 합계
 */
SELECT target_user, 
 create_date,
 COUNT(DISTINCT USER), 
 COUNT(tweet_text),
 SUM(positive_attitude), 
 SUM(negative_attitude), 
 SUM(liwc_wc),
 SUM(liwc_wps),
 SUM(liwc_dic),
 SUM(liwc_sixltr),
 SUM(liwc_pronoun),
 SUM(liwc_i),
 SUM(liwc_we),
 SUM(liwc_self),
 SUM(liwc_you),
 SUM(liwc_other),
 SUM(liwc_negate),
 SUM(liwc_assent),
 SUM(liwc_article),
 SUM(liwc_preps),
 SUM(liwc_number),
 SUM(liwc_affect),
 SUM(liwc_posemo),
 SUM(liwc_posfeel),
 SUM(liwc_optim),
 SUM(liwc_negemo),
 SUM(liwc_anx),
 SUM(liwc_anger),
 SUM(liwc_sad),
 SUM(liwc_cogmech),
 SUM(liwc_cause),
 SUM(liwc_insight),
 SUM(liwc_discrep),
 SUM(liwc_inhib),
 SUM(liwc_tentat),
 SUM(liwc_certain),
 SUM(liwc_senses),
 SUM(liwc_see),
 SUM(liwc_hear),
 SUM(liwc_feel),
 SUM(liwc_social),
 SUM(liwc_comm),
 SUM(liwc_othref),
 SUM(liwc_friends),
 SUM(liwc_family),
 SUM(liwc_humans),
 SUM(liwc_time),
 SUM(liwc_past),
 SUM(liwc_present),
 SUM(liwc_future),
 SUM(liwc_space),
 SUM(liwc_up),
 SUM(liwc_down),
 SUM(liwc_incl),
 SUM(liwc_excl),
 SUM(liwc_motion),
 SUM(liwc_occup),
 SUM(liwc_school),
 SUM(liwc_job),
 SUM(liwc_achieve),
 SUM(liwc_leisure),
 SUM(liwc_home),
 SUM(liwc_sports),
 SUM(liwc_tv),
 SUM(liwc_music),
 SUM(liwc_money),
 SUM(liwc_metaph),
 SUM(liwc_relig),
 SUM(liwc_death),
 SUM(liwc_physcal),
 SUM(liwc_body),
 SUM(liwc_sexual),
 SUM(liwc_eating),
 SUM(liwc_sleep),
 SUM(liwc_groom),
 SUM(liwc_swear),
 SUM(liwc_nonfl),
 SUM(liwc_fillers) 
FROM tweet
WHERE target_user IN (SELECT id FROM USER)
GROUP BY target_user, create_date;


/**
 * 6. Popular User별/날짜별 positive, negative 유저수
 */
SELECT target_user, create_date, COUNT(DISTINCT USER) AS positive_user_count
FROM tweet
WHERE liwc_posemo > 0 AND target_user IN (SELECT id FROM USER)
GROUP BY target_user, create_date;
/*
SELECT target_user, create_date, COUNT(DISTINCT USER) AS positive_user_count
FROM tweet
WHERE (liwc_posemo+liwc_posfeel+liwc_optim) > (liwc_negemo+liwc_anx+liwc_anger+liwc_sad) AND target_user IN (SELECT id FROM USER)
GROUP BY target_user, create_date;
*/

SELECT target_user, create_date, COUNT(DISTINCT USER) AS negative_user_count
FROM tweet
WHERE liwc_negemo > 0 AND target_user IN (SELECT id FROM USER)
GROUP BY target_user, create_date;
/*
SELECT target_user, create_date, COUNT(DISTINCT USER) AS positive_user_count
FROM tweet
WHERE (liwc_posemo+liwc_posfeel+liwc_optim) < (liwc_negemo+liwc_anx+liwc_anger+liwc_sad)AND target_user IN (SELECT id FROM USER)
GROUP BY target_user, create_date;
*/

SELECT target_user, create_date, COUNT(DISTINCT USER) AS positive_negative_user_count
FROM tweet
WHERE (liwc_posemo > 0 AND liwc_negemo > 0) AND target_user IN (SELECT id FROM USER)
GROUP BY target_user, create_date;

SELECT target_user, create_date, COUNT(DISTINCT USER) AS total_user_count
FROM tweet
WHERE (liwc_posemo > 0 OR liwc_negemo > 0) AND target_user IN (SELECT id FROM USER)
GROUP BY target_user, create_date;

/* a9_1 */
SELECT target_user, create_date, COUNT(DISTINCT USER) AS positive_user_count
FROM tweet
WHERE (liwc_posemo+liwc_posfeel+liwc_optim) > (liwc_negemo+liwc_anx+liwc_anger+liwc_sad) 
AND target_user = "BarackObama"
GROUP BY target_user, create_date;

SELECT target_user, create_date, COUNT(DISTINCT USER) AS negative_user_count
FROM tweet
WHERE (liwc_posemo+liwc_posfeel+liwc_optim) <= (liwc_negemo+liwc_anx+liwc_anger+liwc_sad) 
AND ((liwc_negemo+liwc_anx+liwc_anger+liwc_sad) > 0)
AND target_user = "BarackObama"
GROUP BY target_user, create_date;

SELECT target_user, create_date, COUNT(DISTINCT USER) AS total_user_count
FROM tweet
WHERE ((liwc_posemo+liwc_posfeel+liwc_optim) > 0 OR (liwc_negemo+liwc_anx+liwc_anger+liwc_sad) > 0) 
AND target_user = "BarackObama"
GROUP BY target_user, create_date;


/**
 * 8. Tweet타입별 LIWC
 */
SELECT tweet_type, 
 COUNT(DISTINCT USER), 
 COUNT(tweet_text),
 SUM(positive_attitude), 
 SUM(negative_attitude), 
 SUM(liwc_wc),
 SUM(liwc_wps),
 SUM(liwc_dic),
 SUM(liwc_sixltr),
 SUM(liwc_pronoun),
 SUM(liwc_i),
 SUM(liwc_we),
 SUM(liwc_self),
 SUM(liwc_you),
 SUM(liwc_other),
 SUM(liwc_negate),
 SUM(liwc_assent),
 SUM(liwc_article),
 SUM(liwc_preps),
 SUM(liwc_number),
 SUM(liwc_affect),
 SUM(liwc_posemo),
 SUM(liwc_posfeel),
 SUM(liwc_optim),
 SUM(liwc_negemo),
 SUM(liwc_anx),
 SUM(liwc_anger),
 SUM(liwc_sad),
 SUM(liwc_cogmech),
 SUM(liwc_cause),
 SUM(liwc_insight),
 SUM(liwc_discrep),
 SUM(liwc_inhib),
 SUM(liwc_tentat),
 SUM(liwc_certain),
 SUM(liwc_senses),
 SUM(liwc_see),
 SUM(liwc_hear),
 SUM(liwc_feel),
 SUM(liwc_social),
 SUM(liwc_comm),
 SUM(liwc_othref),
 SUM(liwc_friends),
 SUM(liwc_family),
 SUM(liwc_humans),
 SUM(liwc_time),
 SUM(liwc_past),
 SUM(liwc_present),
 SUM(liwc_future),
 SUM(liwc_space),
 SUM(liwc_up),
 SUM(liwc_down),
 SUM(liwc_incl),
 SUM(liwc_excl),
 SUM(liwc_motion),
 SUM(liwc_occup),
 SUM(liwc_school),
 SUM(liwc_job),
 SUM(liwc_achieve),
 SUM(liwc_leisure),
 SUM(liwc_home),
 SUM(liwc_sports),
 SUM(liwc_tv),
 SUM(liwc_music),
 SUM(liwc_money),
 SUM(liwc_metaph),
 SUM(liwc_relig),
 SUM(liwc_death),
 SUM(liwc_physcal),
 SUM(liwc_body),
 SUM(liwc_sexual),
 SUM(liwc_eating),
 SUM(liwc_sleep),
 SUM(liwc_groom),
 SUM(liwc_swear),
 SUM(liwc_nonfl),
 SUM(liwc_fillers) 
FROM tweet
GROUP BY tweet_type;


/*--------------------------------------------------------*/
SELECT target_user, COUNT(id) AS tweet_count
FROM tweet
WHERE  target_user IN (SELECT id FROM USER)
GROUP BY target_user;

SELECT target_user, COUNT(DISTINCT USER) AS positive_user_count
FROM tweet
WHERE (liwc_posemo+liwc_posfeel+liwc_optim) > (liwc_negemo+liwc_anx+liwc_anger+liwc_sad) 
AND target_user IN (SELECT id FROM USER)
GROUP BY target_user;

SELECT target_user, COUNT(id) AS positive_tweet_count
FROM tweet
WHERE (liwc_posemo+liwc_posfeel+liwc_optim) > (liwc_negemo+liwc_anx+liwc_anger+liwc_sad) 
AND target_user IN (SELECT id FROM USER)
GROUP BY target_user;

SELECT target_user, COUNT(DISTINCT USER) AS negative_user_count
FROM tweet
WHERE (liwc_posemo+liwc_posfeel+liwc_optim) <= (liwc_negemo+liwc_anx+liwc_anger+liwc_sad) 
AND ((liwc_negemo+liwc_anx+liwc_anger+liwc_sad) > 0)
AND target_user IN (SELECT id FROM USER)
GROUP BY target_user;

SELECT target_user, COUNT(id) AS negative_tweet_count
FROM tweet
WHERE (liwc_posemo+liwc_posfeel+liwc_optim) <= (liwc_negemo+liwc_anx+liwc_anger+liwc_sad) 
AND ((liwc_negemo+liwc_anx+liwc_anger+liwc_sad) > 0)
AND target_user IN (SELECT id FROM USER)
GROUP BY target_user;

/*--------------------------------------------*/
SELECT target_user, create_date, COUNT(id) AS positive_tweet_count, COUNT(DISTINCT USER) AS positive_user_count
FROM tweet
WHERE (liwc_posemo+liwc_posfeel+liwc_optim) > (liwc_negemo+liwc_anx+liwc_anger+liwc_sad) 
AND target_user IN (SELECT id FROM USER)
GROUP BY target_user, create_date;

SELECT target_user, create_date, COUNT(id) AS negative_tweet_count, COUNT(DISTINCT USER) AS negative_user_count
FROM tweet
WHERE (liwc_posemo+liwc_posfeel+liwc_optim) <= (liwc_negemo+liwc_anx+liwc_anger+liwc_sad) 
AND ((liwc_negemo+liwc_anx+liwc_anger+liwc_sad) > 0)
AND target_user IN (SELECT id FROM USER)
GROUP BY target_user, create_date;

SELECT target_user, create_date, COUNT(id) AS total_pn_tweet_count, COUNT(DISTINCT USER) AS total_pn_user_count
FROM tweet
WHERE ((liwc_posemo+liwc_posfeel+liwc_optim) > 0 OR (liwc_negemo+liwc_anx+liwc_anger+liwc_sad) > 0) 
AND target_user IN (SELECT id FROM USER)
GROUP BY target_user, create_date;

SELECT target_user, create_date, COUNT(id) AS total_tweet_count, COUNT(DISTINCT USER) AS total_user_count
FROM tweet
WHERE target_user IN (SELECT id FROM USER)
GROUP BY target_user, create_date;

