(ns pure.validations-test
  (:use clojure.test pure.validations))


(deftest get-messages-returns-correct-message
  (are [type rule-name param-name messages raw expected] (= (get-message type rule-name param-name messages raw) expected) 
       :s :r :n {:s/r "sr"} "" "sr"
       :s :r :n {:s/r ":name sr"} "" "n sr"
       :s :r :f-n {:s/r ":name sr"} "" "f n sr"
       :s :r :n {:s/r ":raw sr"} "x" "x sr"))


(deftest string-required-checks
  (are [setting val expected] (= (check {:type :string 
                                         :rule [:required setting]
                                         :param [:a val]
                                         :messages {:string/required "sr*"}}) expected)
       false nil nil
       false "" nil
       false "a" nil
       true nil "sr*"
       true "" "sr*"
       true "a" nil))


(deftest string-length-checks
  (are [min max val expected] (= (check {:type :string 
                                         :rule [:length [min max]]
                                         :param [:a val]
                                         :messages {:string/length "sl*"}}) expected)
       nil nil nil nil ;; if no min/max, anything is valid value
       nil nil "any length works" nil
       nil nil "" nil
       2 nil nil "sl*"
       2 nil "" "sl*"
       nil 5 "123456" "sl*"
       nil 5 nil nil
       2 5 nil "sl*"
       2 5 "" "sl*"
       2 5 "12" nil
       2 5 "12345" nil
       2 5 "123456" "sl*"))


(deftest checks-returns-formatted-messages
  (are [type rule val expected] (= (check {:type type
                                           :rule rule
                                           :param [:a val]
                                           :messages {:string/required ":name required"
                                                      :string/length-short "min len :min"
                                                      :string/length-long "max len :max"}}) expected)
       :string [:required true] "" "a required"
       :string [:length [2 5]] "good" nil
       :string [:length [2 5]] "x" "min len 2"
       :string [:length [2 5]] "too long" "max len 5"
       :string [:length [nil 5]] "x" nil
       :string [:length [2 nil]] "xxx" nil))


(deftest string-custom-checks
  (are [val expected] (= (check {:type :string
                                      :rule [:custom #(if (= % "a") "error msg")]
                                      :param [:a val]}) expected)
       nil nil
       "a" "error msg"
       "b" nil))


(deftest int-required-checks
  (are [setting val expected] (= (check {:type :int
                                         :rule [:required setting]
                                         :param [:a val]
                                         :messages {:int/required "ir*"}}) expected)
       false nil nil
       false 1 nil
       false -1 nil
       true nil "ir*"
       true 1 nil
       true -1 nil))
