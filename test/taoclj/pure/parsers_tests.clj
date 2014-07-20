(ns taoclj.pure.parsers-tests
  (:use clojure.test taoclj.pure.parsers))


(deftest strings-are-parsed
  (are [raw expected] (= (parse-string raw nil) expected)
       nil   {:ok true :val nil}
       ""    {:ok true :val nil}
       "a"   {:ok true :val "a"}
       " a " {:ok true :val "a"}
       1     {:ok false}
       {}    {:ok false} ))


(deftest ints-are-parsed
  (are [raw expected] (= (parse-int raw nil) expected)

       nil     {:ok true :val nil}
       ""      {:ok true :val nil}
       1       {:ok true :val 1}
       "1"     {:ok true :val 1}
       "x"     {:ok false :val nil}
       "1.1"   {:ok false :val nil}

       ))

