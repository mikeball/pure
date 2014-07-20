(ns taoclj.pure-tests
  (:require [taoclj.pure :as pure]
            [taoclj.pure.compilation :as compilation]
            [clojure.test :refer [deftest is are]]
            [clj-time.core :as t]))



(defn test-error-and-value [rule raw culture error value]
  (let [result (pure/check (compilation/compile-model {:x rule}) {:x raw} culture)]
         (and (= error (get-in result [:errors :x]))
              (= value (get-in result [:values :x])))))


(deftest string-rules-are-enforced
  (are [rule raw error value]
       (test-error-and-value rule raw :default error value)

       [:string "e*"]                  nil      nil    nil
       [:string "e*"]                  ""       nil    nil
       [:string "e*"]                  "x"      nil    "x"
       [:string "e*"]                  " "      nil    nil
       [:string "e*"]                  " x "    nil    "x"
       [:string "e*"]                  1        "e*"   nil
       [:string "e*"]                  {}       "e*"   nil

       [:string :required "e*"]        nil      "e*"   nil
       [:string :required "e*"]        ""       "e*"   nil
       [:string :required "e*"]        "x"      nil    "x"

       [:string [:length 2 4] "e*"]    nil      nil    nil
       [:string [:length 2 4] "e*"]    ""       nil    nil
       [:string [:length 2 4] "e*"]    "x"      "e*"   "x"
       [:string [:length 2 4] "e*"]    "xx"     nil    "xx"
       [:string [:length 2 4] "e*"]    "xxxx"   nil    "xxxx"
       [:string [:length 2 4] "e*"]    "xxxxx"  "e*"   "xxxxx"

       [:string [:length 2 nil] "e*"]  nil      nil    nil
       [:string [:length 2 nil] "e*"]  ""       nil    nil
       [:string [:length 2 nil] "e*"]  "x"      "e*"   "x"
       [:string [:length 2 nil] "e*"]  "xx"     nil    "xx"
       [:string [:length 2 nil] "e*"]  "xxx"    nil    "xxx"

       [:string [:length nil 2] "e*"]  nil      nil    nil
       [:string [:length nil 2] "e*"]  ""       nil    nil
       [:string [:length nil 2] "e*"]  "xx"     nil    "xx"
       [:string [:length nil 2] "e*"]  "xx "    nil    "xx"
       [:string [:length nil 2] "e*"]  "xxx"   "e*"   "xxx"

       [:string #"x" "e*"]             nil      nil    nil
       [:string #"x" "e*"]             ""       nil    nil
       [:string #"x" "e*"]             "a"      "e*"   "a"
       [:string #"x" "e*"]             "x"      nil    "x"

       ))



(deftest int-rules-are-enforced
  (are [rule raw error value]
       (test-error-and-value rule raw :default error value)

       [:int "e*"]   nil     nil    nil
       [:int "e*"]   ""      nil    nil
       [:int "e*"]   " "     nil    nil
       [:int "e*"]   1       nil    1
       [:int "e*"]   "1"     nil    1
       [:int "e*"]   " 1 "   nil    1
       [:int "e*"]   "x"     "e*"   nil
       [:int "e*"]   "1.1"   "e*"   nil

       [:int :required "e*"]   nil    "e*"   nil
       [:int :required "e*"]   ""     "e*"   nil
       [:int :required "e*"]   "1"    nil    1
       [:int :required "e*"]   "x"    "e*"   nil

       [:int [:range 2 4] "e*"]    nil   nil   nil
       [:int [:range 2 4] "e*"]    ""    nil   nil
       [:int [:range 2 4] "e*"]    "1"   "e*"  1
       [:int [:range 2 4] "e*"]    "2"   nil   2
       [:int [:range 2 4] "e*"]    "4"   nil   4
       [:int [:range 2 4] "e*"]    "5"   "e*"  5

       [:int [:range nil 4] "e*"]  nil   nil   nil
       [:int [:range nil 4] "e*"]  "1"   nil   1
       [:int [:range nil 4] "e*"]  "4"   nil   4
       [:int [:range nil 4] "e*"]  "5"   "e*"  5

       [:int [:range 2 nil] "e*"]  nil   nil   nil
       [:int [:range 2 nil] "e*"]  "1"   "e*"  1
       [:int [:range 2 nil] "e*"]  "2"   nil   2
       [:int [:range 2 nil] "e*"]  "3"   nil   3

       ))




(deftest datetime-rules-are-enforced
  (are [rule raw error value]
       (test-error-and-value rule raw :default error value)

       [:datetime "MM/dd/yyyy" "e*"]   nil          nil   nil
       [:datetime "MM/dd/yyyy" "e*"]   ""           nil   nil
       [:datetime "MM/dd/yyyy" "e*"]   1            "e*"  nil
       [:datetime "MM/dd/yyyy" "e*"]   "7/14/2014"  nil   (t/date-time 2014 7 14)

       ; :required todo
       ; :range condition just like int?
       ; or this? [:datetime "yyyy-MM-dd" [:before :hours -10] "e*"]

       ))



;; (deftest email-rules-are-enforced
;;   (are [rule raw expected]
;;        (= ((r/rule-fn :id rule) raw) expected)

;;        [:email "e*"]            nil          {:ok true :val nil}
;;        [:email "e*"]            ""           {:ok true :val nil}
;;        [:email "e*"]            " "          {:ok true :val nil}
;;        [:email "e*"]            "a@b.com"    {:ok true :val "a@b.com"}
;;        [:email "e*"]            " a@b.com "  {:ok true :val "a@b.com"}
;;        [:email "e*"]            "x"          {:ok false :msg "e*" :val "x"}

;;        [:email :required "e*"]  nil                {:ok false :msg "e*" :val nil}
;;        [:email :required "e*"]  ""                 {:ok false :msg "e*" :val nil}
;;        [:email :required "e*"]  "a@b.com"          {:ok true :val "a@b.com"}
;;        [:email :required "e*"]  "tag+a@b.com"      {:ok true :val "tag+a@b.com"}
;;        [:email :required "e*"]  "x"                {:ok false :msg "e*" :val "x"}
;;        [:email :required "e*"]  "a@b.none"         {:ok false :msg "e*" :val "a@b.none"}
;;        [:email :required "e*"]  "a@b.com,@a@b.com" {:ok false :msg "e*" :val "a@b.com,@a@b.com"}

;;        ))




(deftest custom-conditions-are-enforced
  (are [rule raw error]
       (test-error-and-value rule raw :default error nil)

       ; custom conditions that return true indicate pass
       [:string (fn [_ _] true) "e*"]     ""   nil

       ; any non-true result from custom conditions are failures
       [:string (fn [_ _] nil) "e*"]      ""   "e*"
       [:string (fn [_ _] false) "e*"]    ""   "e*"

       ; string results from custom conditions are considered error messages
       [:string (fn [_ _] "msg*") "e*"]   ""   "msg*"
  ))







(deftest errors-are-localized
  (are [rule raw culture error]
       (test-error-and-value rule raw culture error nil)

       ; string in last postition of rule used for default message
       [:int "en*"]
       "x" :default  "en*"

       ; uknown cultures fall back to default
       [:int {:default "en*" :de-de "de*"}]
       "x" :xxx  "en*"

       ; localized error messages are returned
       [:int {:default "en*" :de-de "de*"}]
       "x" :de-de  "de*"


       ))


(deftest datetime-handles-localized-formats
  (are [rule raw culture error value]
       (test-error-and-value rule raw culture error value)

       [:datetime {:default "MM/dd/yyyy"} "e*"]
       "7/14/2014" :default  nil   (t/date-time 2014 7 14)

       [:datetime {:de-de "yyyy-MM-dd"} "e*"]
       "2014-07-14" :de-de nil   (t/date-time 2014 7 14)

       ))


(deftest custom-conditions-are-passed-culture-and-value
  (is (= {:name "x :de-de"}

         (:errors
          (pure/check
            (compilation/compile-model
              {:name [:string (fn [value culture] (str value " " culture)) "e*"]})
            {:name "x"}
             :de-de))

         )))



(deftest nested-models-are-checked
  (is (= {:street {:address "e*"}}
         (:errors (pure/check
                   (compilation/compile-model
                    {:street {:address [:string :required "e*"]}})

                   {:street {:address ""}}))
         ))

  (is (= {:l1 {:l2 {:l3 "e*"}}}
         (:errors (pure/check
                   (compilation/compile-model
                    {:l1 {:l2 {:l3 [:string :required "e*"]}}})

                   {:l1 {:l2 {:l3 ""}}}))

         )))





(deftest complete-models-are-checked-and-return-expected-keys
  (is (= {:errors {:name "name e*" :age "age e*" }
          :raw    {:name 1 :age "x"}
          :values {:name nil :age nil}}

         (pure/check
          (compilation/compile-model
            {:name [:string "name e*"]
             :age [:int "age e*"]})

          {:name 1 :age "x"})

         )))






;; (compilation/compile-model {:age [:string (fn [_ _] nil)    "e*"]}
;;       )

;; (pure/check (compilation/compile-model {:age [:string (fn [_ _] nil)    "e*"]})
;;        {:age ""}
;;        )

;; (pure/check (compilation/compile-model {:birth [:datetime "MM/dd/yyyy" "e*"]}))

; (run-tests 'taoclj.pure-tests
; )














