(ns taoclj.pure.parsing-tests
  (:use clojure.test taoclj.pure.parsing))


(deftest parse-strings
  (are [type raw expected] (= (parse type raw nil) expected)

       :string nil   {:ok true :val nil}
       :string ""    {:ok true :val nil}
       :string "a"   {:ok true :val "a"}
       :string " a " {:ok true :val "a"}
       :string 1     {:ok false}
       :string {}    {:ok false}

       ))


(deftest parse-ints
  (are [type raw expected] (= (parse type raw nil) expected)

       :int nil     {:ok true :val nil}
       :int ""      {:ok true :val nil}
       :int 1       {:ok true :val 1}
       :int "1"     {:ok true :val 1}
       :int "x"     {:ok false :val nil}
       :int "1.1"   {:ok false :val nil}

       ))

