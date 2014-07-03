(ns taoclj.pure.rules-tests
  (:require [taoclj.pure.rules :as r]
            [clojure.test :refer [deftest is are]]))


(deftest string-rules-are-enforced
  (are [rule raw expected]
       (= ((r/rule-fn :id rule) raw) expected)

       [:string "e*"]               nil      {:ok true :val nil}
       [:string "e*"]               ""       {:ok true :val nil}
       [:string "e*"]               "x"      {:ok true :val "x"}
       [:string "e*"]               " "      {:ok true :val nil}
       [:string "e*"]               " x "    {:ok true :val "x"}
       [:string "e*"]               1        {:ok false :msg "e*"}
       [:string "e*"]               {}       {:ok false :msg "e*"}

       [:string :required "e*"]     nil      {:ok false :msg "e*" :val nil}
       [:string :required "e*"]     ""       {:ok false :msg "e*" :val nil}

       [:string [:length 2 4] "e*"] nil      {:ok true :val nil}
       [:string [:length 2 4] "e*"] ""       {:ok true :val nil}
       [:string [:length 2 4] "e*"] "x"      {:ok false :msg "e*" :val "x"}
       [:string [:length 2 4] "e*"] "xx"     {:ok true :val "xx"}
       [:string [:length 2 4] "e*"] "xxxx"   {:ok true :val "xxxx"}
       [:string [:length 2 4] "e*"] "xxxxx"  {:ok false :msg "e*" :val "xxxxx"}

       [:string [:length 2 nil] "e*"]  nil    {:ok true :val nil}
       [:string [:length 2 nil] "e*"]  ""     {:ok true :val nil}
       [:string [:length 2 nil] "e*"]  "x"    {:ok false :msg "e*" :val "x"}
       [:string [:length 2 nil] "e*"]  "xx"   {:ok true :val "xx"}
       [:string [:length 2 nil] "e*"]  "xxx"  {:ok true :val "xxx"}

       [:string [:length nil 2] "e*"]  nil    {:ok true :val nil}
       [:string [:length nil 2] "e*"]  ""     {:ok true :val nil}
       [:string [:length nil 2] "e*"]  "xx"   {:ok true :val "xx"}
       [:string [:length nil 2] "e*"]  "xx "  {:ok true :val "xx"}
       [:string [:length nil 2] "e*"]  "xxx"  {:ok false :msg "e*" :val "xxx"}
       ))


(deftest integer-rules-are-enforced
  (are [rule raw expected]
       (= ((r/rule-fn :id rule) raw) expected)

       [:int "e*"]               nil      {:ok true :val nil}
       [:int "e*"]               ""       {:ok true :val nil}
       [:int "e*"]               " "      {:ok true :val nil}
       [:int "e*"]               "1"      {:ok true :val 1}
       [:int "e*"]               " 1 "    {:ok true :val 1}
       [:int "e*"]               "1.1"    {:ok false :msg "e*" :val nil}
       [:int "e*"]               "x"      {:ok false :msg "e*" :val nil}

       [:int :required "e*"]     nil      {:ok false :msg "e*" :val nil}
       [:int :required "e*"]     ""       {:ok false :msg "e*" :val nil}
       [:int :required "e*"]     "1"      {:ok true :val 1}
       [:int :required "e*"]     "x"      {:ok false :msg "e*" :val nil}

       [:int [:range 2 4] "e*"]  nil      {:ok true :val nil}
       [:int [:range 2 4] "e*"]  ""       {:ok true :val nil}
       [:int [:range 2 4] "e*"]  "1"      {:ok false :msg "e*" :val 1}
       [:int [:range 2 4] "e*"]  "2"      {:ok true :val 2}
       [:int [:range 2 4] "e*"]  "4"      {:ok true :val 4}
       [:int [:range 2 4] "e*"]  "5"      {:ok false :msg "e*" :val 5}

       [:int [:range nil 4] "e*"]  nil    {:ok true :val nil}
       [:int [:range nil 4] "e*"]  "1"    {:ok true :val 1}
       [:int [:range nil 4] "e*"]  "4"    {:ok true :val 4}
       [:int [:range nil 4] "e*"]  "5"    {:ok false :msg "e*" :val 5}

       [:int [:range 2 nil] "e*"]  nil    {:ok true :val nil}
       [:int [:range 2 nil] "e*"]  "1"    {:ok false :msg "e*" :val 1}
       [:int [:range 2 nil] "e*"]  "2"    {:ok true :val 2}
       [:int [:range 2 nil] "e*"]  "3"    {:ok true :val 3}

       ))


(deftest email-rules-are-enforced
  (are [rule raw expected]
       (= ((r/rule-fn :id rule) raw) expected)

       [:email "e*"]            nil          {:ok true :val nil}
       [:email "e*"]            ""           {:ok true :val nil}
       [:email "e*"]            " "          {:ok true :val nil}
       [:email "e*"]            "a@b.com"    {:ok true :val "a@b.com"}
       [:email "e*"]            " a@b.com "  {:ok true :val "a@b.com"}
       [:email "e*"]            "x"          {:ok false :msg "e*" :val "x"}

       [:email :required "e*"]  nil                {:ok false :msg "e*" :val nil}
       [:email :required "e*"]  ""                 {:ok false :msg "e*" :val nil}
       [:email :required "e*"]  "a@b.com"          {:ok true :val "a@b.com"}
       [:email :required "e*"]  "tag+a@b.com"      {:ok true :val "tag+a@b.com"}
       [:email :required "e*"]  "x"                {:ok false :msg "e*" :val "x"}
       [:email :required "e*"]  "a@b.none"         {:ok false :msg "e*" :val "a@b.none"}
       [:email :required "e*"]  "a@b.com,@a@b.com" {:ok false :msg "e*" :val "a@b.com,@a@b.com"}

       ))


(deftest rule-messages-are-localized
  (are [rule raw culture expected]
       (= expected ((r/rule-fn :id rule) raw culture))

       ; string in last postition of rule used for default message
       [:string "e*"] 1  :xx  {:ok false :msg "e*"}

       ; uknown cultures fall back to default
       [:string {:default "e*"
                 :de-de "das e*"}] 1  :xx  {:ok false :msg "e*"}

       ; localized error messages are returned
       [:string {:default "e*"
                 :de-de "das e*"}] 1  :de-de  {:ok false :msg "das e*"}

       ))


(deftest custom-conditions-are-enforced
  (are [rule raw expected]
       (= expected ((r/rule-fn :id rule) raw))

       [:string (fn [_ _] nil) "e*"]
       ""
       {:ok true :val nil}

       [:string (fn [_ _] "custom error msg") "e*"]
       ""
       {:ok false :msg "custom error msg" :val nil}

       ))


(deftest custom-conditions-are-passed-culture-and-value-and-message-is-returned
  (is (= {:ok false :msg ":de-de x" :val "x"}
         ((r/rule-fn :id [:string (fn [culture value] (str culture " " value)) "e*"])
           "x" :de-de))))










