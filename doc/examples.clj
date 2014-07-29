(require '[taoclj.pure :as pure :refer [defm check]])



; ## A Quick Intro

; Create a model with some simple rules
(defm model
  {:name [:string :required [:length 3 10]
          "Name is required and must be between 3 and 10 characters long."]

   :age [:int [:range 21 130] "Age is optional but must be at least 21"]})

; you can also use the taoclj.pure.compilation/compile-model function to build a model like this
; (compile-model {:name [:string :required "error"]})



; Let's check some invalid data
(check model {:name " bo " :age "18"})

=> {:errors {:age "Age is optional but must be at least 21"
             :name "Name is required and must be between 3 and 10 characters long."},
    :raw {:name " bo ", :age "18"}
    :values {:age 18, :name "bo"}}




; Let's check some valid data
(check model {:name " bob " :age "21"})

=> {:raw    {:name "bob" :age "21"}
    :values {:name "bob" :age 21}}

; * notice the name is trimmed and age is parsed to integer!




; ## Compilation of models

; the simplest way to compile a model is by using the defm macro
(defm my-model
  {:somekey [:int "Somekey must be an integer"]})


; You can also use the compile-model fuction rather than the defm macro
(def my-model
  (taoclj.pure.compilation/compile-model
   {:somekey [:int "Somekey must be an integer"]}))




; ## String Validations

; a model for a simple string
(defm string-model
  {:name [:string :required [:length 2 4] "Name must be between 2 and 4 characters long"]})

; a name that's too short, note whitespace is trimmed
(check string-model {:name " x "})
=> {:errors {:name "Name must be between 2 and 4 characters"}
    :raw    {:name " x "}
    :values {:name "x"}}

; a valid name
(check string-model {:name " xx "})

=> {:raw {:name " xx "}
    :values {:name "xx"}}


; a name that's too long
(check string-model {:name "xxxxx"})
=> {:errors {:name "Name must be between 2 and 4 characters"}
    :raw    {:name "xxxxx"}
    :values {:name "xxxxx"}}


; Non string key values are considered invalid
(check string-model {:name 1})

=> {:errors {:name "Name must be between 2 and 4 characters"}
    :raw    {:name 1}
    :values {:name nil}}



; a model with a regular expression condition
(defm string-regx-model
  {:name [:string #"z.*" "Name must start with z"]})

; here we check a name that fails the regex condition
(check string-regx-model {:name "xxx"})

=> {:errors {:name "Name must start with z"}
    :raw    {:name "xxx"}
    :values {:name "xxx"}}


; and here we check a name that passes the regex
(check string-regx-model {:name "zxx"})

=> {:raw    {:name "zxx"}
    :values {:name "zxx"}}




; ## Int Validation

; todo






; ## Datetime Validation

; let's define a simple model with a datetime
; datetimes require a date format that directly follows the datatype in the rule
(defm dt-model
  {:start [:datetime "MM/dd/yyyy" "Start date required in mm/dd/yyyy format"]})


; an invalid date string triggers an error
(check dt-model {:start "x"})

=> {:errors {:start "Start date required in mm/dd/yyyy format"}
    :raw    {:start "x"}
    :values {:start nil}}


; valid date returns parsed DateTime object
(check dt-model {:start "7/28/2014"})

=> {:raw    {:start "7/28/2014"}
    :values {:start #<DateTime 2014-07-28T00:00:00.000Z>}}


; ** please see localization section for how to handle localized date formats




; ## Nested Validation

; todo




; ## Cross Field Validation

; todo





; ## Localization

; Define a model with localized error messages.
; A :default message is required and used for fallback

(defm localized-model
  {:a [:string :required {:default "error"
                          :de-de "Fehler"}]})

; A check uses the passed culture code pick error message
(check localized-model {:a ""} :de-de)

=> {:errors {:a "Fehler"}
    :raw    {:a ""}
    :values {:a nil}}


; Unknown culture-codes fall back to default text
(check localized-model {:a ""} :fr-fr)

=> {:errors {:a "error"}
    :raw    {:a ""}
    :values {:a nil}}



; Custom conditions are functions with 3 parameters.
; * note entire parsed map and culture-code are passed for use if needed.
(defn my-condition [parsed-map parsed-value culture-code]
  (cond (= parsed-value "good")    true
        (= parsed-value "notgood") false
        :default                   (str "Error " culture-code)))

; define a model that uses the custom condtion
(defm localized-custom-model
  {:a [:string my-condition {:default "error"
                             :de-de "Fehler"}]})

; if your custom condition returns a string, it's considered
; a failure and the string is used as the error message
(check localized-custom-model {:a "bad"} :de-de)

=> {:errors {:a "Error :de-de"}
    :raw    {:a "bad"}
    :values {:a "bad"}}


; if your custom condition returns anything besides true or string,
; it's considered a failure and uses standard error message
(check localized-custom-model {:a "notgood"} :de-de)

=> {:errors {:a "Fehler"}
    :raw    {:a "notgood"}
    :values {:a "notgood"}}


; if your custom condition returns true, it's considered valid.
(check localized-custom-model {:a "good"} :de-de)

=> {:raw    {:a "good"}
    :values {:a "good"}}



; Datetime Localization

; define a model with localized datetime formats and error messages
(defm localized-datetime-model
  {:start [:datetime
           {:default "MM/dd/yyyy" :de-de "yyyy-MM-dd"}
           {:default "Start must be a valid date - mm/dd/yyyy"
            :de-de   "Starten muss ein gültiges Datum sein - yyyy-mm-dd"}]})


; an invalid date using default culture fallbacks
(check localized-datetime-model {:start "x"})

=> {:errors {:start "Start must be a valid date - mm/dd/yyyy"}
    :raw    {:start "x"}
    :values {:start nil}}


; a valid date using default culture
(check localized-datetime-model {:start "7/28/2014"})

=> {:raw {:start "7/28/2014"}
    :values {:start #<DateTime 2014-07-28T00:00:00.000Z>}}


; an invalid date using specific culture code
(check localized-datetime-model {:start "x"} :de-de)

=> {:errors {:start "Starten muss ein gültiges Datum sein - yyyy-mm-dd"}
    :raw    {:start "x"}
    :values {:start nil}}


; a valid date using specific culture
(check localized-datetime-model {:start "2014-7-28"} :de-de)

=> {:raw    {:start "2014-7-28"}
    :values {:start #<DateTime 2014-07-28T00:00:00.000Z>}}









