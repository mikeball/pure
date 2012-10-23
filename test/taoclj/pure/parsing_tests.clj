(ns taoclj.pure.parsing-tests
  (:use clojure.test taoclj.pure.parsing))


(deftest parse-types
  (are [type raw expected] (= (parse type raw) expected)
       :string nil nil
       :string "" ""
       :string "a" "a"
       :string " a " "a"
       :int nil nil
       :int 1 1
       :int "1" 1
       :int "" nil
       :int "x" nil
       :int "1.1" nil))