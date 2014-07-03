(ns taoclj.pure-tests
  (:require [taoclj.pure :as pure]
            [clojure.test :refer [deftest is are]]))


(deftest models-keys-are-compiled-and-check
  (are [model val expected]
       (= expected ((-> (pure/compile-model model) :a) val))

       {:a [:string :required "e*"]}
       ""
       {:ok false :msg "e*" :val nil}

       {:a [:string :required "e*"]}
       "x"
       {:ok true :val "x"}

       ))



(deftest entire-models-are-compiled-checked

  (let [model (pure/compile-model {:a [:string :required "e1"]
                                   :b [:int :required "e2"]})]
    (are [params expected]
         (= (pure/check model params) expected)

         {}
         {:errors {:a "e1" :b "e2"} :raw {}}

         {:a "x" :b ""}
         {:errors {:b "e2"} :raw {:a "x" :b ""}}

         {:a "x" :b "1"}
         {:values {:a "x" :b 1} :raw {:a "x" :b "1"}}

         )))
















