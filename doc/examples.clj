
(require '[taoclj.pure :as pure :refer [defm check]])


; Create a model with some simple rules
(defm model
  {:name [:string :required [:length 3 10]
          "Name is required and must be between 3 and 10 characters long."]

   :age [:int [:range 21 130] "Age is optional but must be at least 21"]})

; you can also use the pure/compile-model function to build a model like this
; (pure/compile-model {:name [:string :required "error"]})



; Let's check some invalid data
(check model {:name " bo " :age "18"})

=> {:errors {:age "Age is optional but must be at least 21"
             :name "Name is required and must be between 3 and 10 characters long."}
    :raw {:name " bo ", :age "18"}}


; Let's check some valid data
(check model {:name "bob" :age "21"})

=> {:values {:age 21 :name "bob"}
    :raw {:name "bob" :age "21"}}




; A localized message
(defm localized-model
  {:a [:string :required {:default "error"
                          :de-de "Fehler"}]})

; A check uses the passed culture code pick error message
(check localized-model {:a ""} :de-de)
=> {:errors {:a "Fehler"}, :raw {:a ""}}

; Unknown culture-codes fall back to default text
(check localized-model {:a ""} :fr-fr)
=> {:errors {:a "error"}, :raw {:a ""}}



; The culture-code is passed to custom condition functions as well.
(defn my-condition [culture-code parsed-value]
  (if (= parsed-value "good") nil
    (str "My custom error determined by culture-code " culture-code)))

(defm localized-custom-model
  {:a [:string my-condition {:default "error"
                             :de-de "Fehler"}]})

(check localized-custom-model {:a "bad"} :de-de)
(check localized-custom-model {:a "good"} :de-de)

(defn my-condition [parsed-value culture source]
  (if (= parsed-value "good") nil
    (str "My custom error determined by culture-code " culture-code)))

{:culture :de-de
 :source  original-map
 }






