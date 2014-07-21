# Pure

Pure is a validation, and parameter parsing library for clojure. One of the most tedious parts of building applications is validating & parsing string based parameters into the values your application needs such as integers and datetimes. Pure allows you to specify a model with the type of an attribute, as well as a set of conditions, then check a map of data against the rules and have data parsed as well.

Status : experimental with major changes likely.


```clojure
[org.taoclj/pure "0.0.3"]
```

[![Continuous Integration status](https://secure.travis-ci.org/mikeball/pure.png)](http://travis-ci.org/mikeball/pure)


## Usage
```clojure
(require '[taoclj.pure :refer [defm check]])


; Create a model with some simple rules
(defm model
  {:name [:string :required [:length 3 10]
          "Name is required and must be between 3 and 10 characters long."]

   :age [:int [:range 21 130] "Age is optional but must be at least 21"]})


; A check with invalid data
(check model {:name "bo " :age "18"})

=> {:errors {:name "Name is required and must be between 3 and 10 characters long."
             :age "Age is optional but must be at least 21" }
    :raw {:name "bo ", :age "18"}}


; A check with valid data
(check model {:name "bob " :age "21"})

=> {:values {:name "bob" :age 21}
    :raw {:name "bob " :age "21"}}

    ; * note the name is trimmed and age is parsed to integer


```


## Rules

Rules are simply a vector with the first element being the type such as :string or :int and the last being the error message. Conditions are optional and multiple conditions can be added between the type and the message.

```clojure
; Rule format
[datatype condition1 condition2 conditionX failure-message]
```




## Conditions
```clojure

; :string conditions
:required         ; you must supply a non-blank string
[:length 2 nil]   ; must be at least 2 or more characters long
[:length nil 5]   ; must be 5 or less characters long
[:length 2 5]     ; must be between 2 and 5 characters long
#"x"              ; must match the regular expression

; :int conditions
:required         ; you must supply a valid integer
[:range 2 nil]    ; must be an integer 2 or greater
[:range nil 5]    ; must be an integer 5 or less
[:range 2 5] }    ; must be an integer between 2 and 5


; :email conditions
:required         ; you must supply a valid email address


; Custom conditions are simply functions you write.
; They must have an arity of 2.
; The function should return true if the value is ok.
; If you return a string, it will be considered a failure
; and used as the error message. All other responses are considered
; failures and will use the regular error messages.

(fn [parsed-value culture-code]
  (if no-error true
               "A message describing the problem"))


```




## Localized Error Messages

You can localize error messages by passing a map rather than a string as the last element of the
rule. You must include a default key and any other culture codes you would like.

```clojure
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



; The culture-code is passed to custom condition functions.
(defn my-condition [parsed-value culture-code]
  (if (= parsed-value "good") true
    (str "My custom error determined by culture-code " culture-code)))

(defm localized-custom-model
  {:a [:string my-condition {:default "error"
                             :de-de "Fehler"}]})

(check localized-custom-model {:a "bad"} :de-de)
=> {:errors {:a "My custom error determined by culture-code :de-de"} :raw {:a "bad"}}

(check localized-custom-model {:a "good"} :de-de)
=> {:values {:a "good"} :raw {:a "good"}}

```


## TODO

- date/time support
- before/after date condition
- validation across/between keys, eg validate passwords match
- multi-level or sub item validation
- phone number type
- postal code type
- ISO country code type
- US state type
- collection validation
- condition exists in set/list [:oneof :a :b :c "err"]









## License

Copyright © 2014 Michael Ball

Distributed under the Eclipse Public License, the same as Clojure.
