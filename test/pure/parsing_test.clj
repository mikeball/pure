(ns pure.parsing-test
  (:use clojure.test pure.parsing))


(deftest parse-types
  (are [type raw expected] (= (parse type raw) expected)
       :unknown "a" "a"
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