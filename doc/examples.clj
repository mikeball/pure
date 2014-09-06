(require '[taoclj.pure :as pure :refer [defm check]])



; ## A Quick Intro

; Create a model with some rules
(defm model
  {:name [:string :required [:length 3 10]
          "Name is required and must be between 3 and 10 characters."]

   :age [:int [:range 21 130] "Age is optional but must be at least 21"]})


; Let's check some invalid data
(check model {:name " bo " :age "18"})

=> {:errors {:age "Age is optional but must be at least 21"
             :name "Name is required and must be between 3 and 10 characters."}
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
  {:name [:string :required [:length 2 4]
          "Name must be between 2 and 4 characters"]})

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


; Non string values are considered invalid
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


(defm int-model
  {:age [:int :required [:range 21 130]
         "Age is required and must between 21 and 130"]})


; age is required
(check int-model {:age ""})

=> {:errors {:age "Age is required and must between 21 and 130"}
    :raw    {:age ""}
    :values {:age nil}}


; age must be a valid integer
(check int-model {:age "x"})
=> {:errors {:age "Age is required and must between 21 and 130"}
    :raw    {:age "x"}
    :values {:age nil}}


; age must be within the specified range
(check int-model {:age "20"})

=> {:errors {:age "Age is required and must between 21 and 130"}
    :raw    {:age "20"}
    :values {:age 20}}


; finally a valid age
(check int-model {:age "21"})

=> {:raw {:age "21"}
    :values {:age 21}}







; ## Datetime Validation

; Define a simple model with a datetime.
; Datetimes require a date format string that directly follows the datatype in the rule
; and also requires a time component in the format string. The string, if valid, is parsed into
; a java.time.LocalDateTime object.
(defm dt-model
  {:start [:datetime "MM/dd/yyyy HH:mm" "Start date required in MM/dd/yyyy HH:mm format"]})


; an invalid date string triggers an error
(check dt-model {:start "x"})

=> {:errors {:start "Start date required in MM/dd/yyyy HH:mm format"}
    :raw    {:start "x"}
    :values {:start nil}}


; valid date returns parsed DateTime object
(check dt-model {:start "07/28/2014 10:11"})

=> {:raw    {:start "07/28/2014 10:11"}
    :values {:start #<LocalDateTime 2014-07-28T10:11>}}



; ** please see localization section for how to handle localized date formats



; ## Email Validation

(defm email-model
  {:contact [:email :required "Please supply a valid email contact address"]})

; email is required
(check email-model {:contact ""})

=> {:errors {:contact "Please supply a valid email contact address"}
    :raw    {:contact ""}
    :values {:contact nil}}


; email must be valid
(check email-model {:contact "x@x.x"})

=> {:errors {:contact "Please supply a valid email contact address"}
    :raw    {:contact "x@x.x"}
    :values {:contact nil}}


; valid emails are cleaned and allowed
(check email-model {:contact " x@x.com "})

=> {:raw    {:contact " x@x.com "}
    :values {:contact "x@x.com"}}






; ## Nested Validation


(defm nested-model
  {:address {:street [:string :required "Address is required"]}})

; street is required, and nested error messages are present
(check nested-model {})
=> {:errors {:address {:street "Address is required"}}
    :raw    {}
    :values {:address {:street nil}}}


; a valid address is allowed
(check nested-model {:address {:street " 123 Oak "}})
=> {:raw    {:address {:street " 123 Oak "}}
    :values {:address {:street "123 Oak"}}}




; ## Cross Field Validation

(defm cross-model
  {:password [:string :required [:length 6 10] "Password is required"]
   :confirm  [:string [= :password] "Password confirmation doesn't match"]})


(check cross-model {:password "abc123" :confirm "x"})

=> {:errors {:confirm "Password confirmation doesn't match"}
    :raw    {:password "abc123" :confirm "x"}
    :values {:confirm "x" :password "abc123"}}



(check cross-model {:password "abc123" :confirm "abc123"})

=> {:raw    {:password "abc123" :confirm "abc123"}
    :values {:password "abc123" :confirm "abc123"}}



(defm int-cross-model
  {:low  [:int :required   "Low is required"]
   :high [:int [> :low] "High must be greater than low"]})


(check int-cross-model {:low "2" :high "1"})
=> {:errors {:high "High must be greater than low"}
    :raw    {:low "2" :high "1"}
    :values {:low 2   :high 1 }}


(check int-cross-model {:low "2" :high "3"})

=> {:raw    {:low "2" :high "3"}
    :values {:low 2   :high 3}}




; Datetime before and after conditions

(defm cross-datetime-model
  {:start [:datetime "MM/dd/yyyy HH:mm" "Start must be a valid date"]
   :end   [:datetime "MM/dd/yyyy HH:mm" [:after :start] "End must be after start"]})


(check cross-datetime-model {:start "07/28/2014 10:11" :end "07/27/2014 10:11"})

=> {:errors {:end "End must be after start"}
    :raw    {:start "07/28/2014 10:11" :end "07/27/2014 10:11"}
    :values {:end #<LocalDateTime 2014-07-27T10:11> :start #<LocalDateTime 2014-07-28T10:11>}}




(check cross-datetime-model {:start "07/28/2014 10:11" :end "07/29/2014 10:11"})

=> {:raw    {:start "07/28/2014 10:11", :end "07/29/2014 10:11"}
    :values {:end #<LocalDateTime 2014-07-29T10:11>, :start #<LocalDateTime 2014-07-28T10:11>}}


; Referencing nested fields
(defm nested-cross-model
  {:address {:street1 [:string :required "Street address is required"] }

   :billing {:street2 [:string [= :address :street1] "Billing address must match"]}})

(check nested-cross-model
       {:address {:street1 "123 Oak"}
        :billing {:street2 "x"}})
=> {:errors {:billing {:street2 "Billing address must match"}}
    :raw    {:address {:street1 "123 Oak"} :billing {:street2 "x"}}
    :values {:address {:street1 "123 Oak"} :billing {:street2 "x"}}}


(check nested-cross-model
       {:address {:street1 "123 Oak"}
        :billing {:street2 "123 Oak"}})

=> {:raw    {:address {:street1 "123 Oak"} :billing {:street2 "123 Oak"}}
    :values {:address {:street1 "123 Oak"} :billing {:street2 "123 Oak"}}}









; ## Localization of error messages

; Define a model with localized error messages,
; a :default message is required and used for fallback
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






; ## Localization of datetime formats

; define a model with localized datetime formats and error messages
(defm localized-datetime-model
  {:start [:datetime
           {:default "MM/dd/yyyy HH:mm" :de-de "yyyy-MM-dd HH:mm"}
           {:default "Start must be a valid date - MM/dd/yyyy HH:mm"
            :de-de   "Starten muss ein gültiges Datum sein - yyyy-MM-dd HH:mm"}]})


; an invalid date using default culture fallbacks
(check localized-datetime-model {:start "x"})

=> {:errors {:start "Start must be a valid date - MM/dd/yyyy HH:mm"}
    :raw    {:start "x"}
    :values {:start nil}}


; a valid date using default culture
(check localized-datetime-model {:start "07/28/2014 10:11"})

=> {:raw    {:start "07/28/2014 10:11"}
    :values {:start #<LocalDateTime 2014-07-28T10:11>}}



; an invalid date using specific culture code
(check localized-datetime-model {:start "x"} :de-de)

=> {:errors {:start "Starten muss ein gültiges Datum sein - yyyy-MM-dd HH:mm"}
    :raw    {:start "x"}
    :values {:start nil}}




; a valid date using specific culture
(check localized-datetime-model {:start "2014-07-28 10:11"} :de-de)

=> {:raw    {:start "2014-07-28 10:11"}
    :values {:start #<LocalDateTime 2014-07-28T10:11>}}










; ## Localization of custom conditions

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





; ## Models allow string keys

(defm stringkey-model
  {"address" {"street" [:string :required "Street address is required"]}
   "age" [:int "Age must be an integer"]})


; a check with invalid data
(check stringkey-model {"address" {"street" ""}
                        "age" "x"})

=> {:errors {"address" {"street" "Street address is required"}, "age" "Age must be an integer"}
    :raw {"address" {"street" ""}, "age" "x"}
    :values {"age" nil, "address" {"street" nil}}}


; a check with valid data
(check stringkey-model {"address" {"street" "123 Oak"}
                        "age" "21"})

=> {:raw {"address" {"street" "123 Oak"}, "age" "21"}
    :values {"age" 21, "address" {"street" "123 Oak"}}}



