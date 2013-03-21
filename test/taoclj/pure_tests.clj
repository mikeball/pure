(ns taoclj.pure-tests
  (:use clojure.test taoclj.pure))


(deftest check-rules-order
  (are [param expected] 
       (= (check-rules {:type :string
                        :rules {:required true :length [2 5]}
                        :messages {:string/required "r*" 
                                   :string/length-short "ls*"}
                        :param param}) expected)
       [:a nil] [nil "r*"] ;; required is tested first
       [:a "1"] ["1" "ls*"] )) ;; length tested second


(deftest required-check-rules
  (are [type required param expected] 
       (= (check-rules {:type type
                        :rules {:required required}
                        :messages {:string/required "sr" :int/required "ir"}
                        :param param}) expected)
       :string true [:a "a"] ["a" nil]
       :string true [:a nil] [nil "sr"]
       :int true [:a "1"] [1 nil]
       :int true [:a nil] [nil "ir"]
       :int false [:a "1"] [1 nil] ))


(def messages {:string/required "string required" 
               :string/length-short "string short"
               :string/length-long "long"
               :int/required "int required" 
               :int/range "range"
               :email/required "email required"
               :email/invalid "email invalid"})

(deftest validate-errors-are-returned
  (are [params model expected] (= (:errors (validate params model messages)) expected)
       {:a ""} {:a {:type :string :required true}} {:a "string required"}
       {:a "a"} {:a {:type :string :length [2 5]}} {:a "string short"}
       {:a "good"} {:a {:type :string :required true :length [2 5]}} nil
       {:a "a"} {:a {:type :string :custom #(if (= % "a") "nope")}} {:a "nope"}
       {:a ""} {:a {:type :int :required true}} {:a "int required"}
       {:a ""} {:a {:type :email :required true}} {:a "email required"}
       {:a "abc"} {:a {:type :email :required false}} {:a "email invalid"}))


(deftest validate-values-are-parsed
  (are [params model expected] (= (:values (validate params model messages)) expected)
       {:a "good"} {:a {:type :string :required true}} {:a "good"}
       {:a "1"} {:a {:type :int :required true}} {:a 1}))






