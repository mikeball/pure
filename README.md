# Pure

Pure is a validation, and parameter parsing library for clojure. 

One of the most tedious parts of building applications is validating & parsing string based parameters into the values your application needs such as integers and datetimes. Pure allows you to specify a model with the type of an attribute, as well as validation rules, then have the parsing and validation performed automatically.


[![Continuous Integration status](https://secure.travis-ci.org/mikeball/pure.png)](http://travis-ci.org/mikeball/pure)


## Installation

Add the following dependency to your `project.clj` file:

```clojure
[org.taoclj/pure "0.0.1"]
```


## Usage

The main validation function is `taoclj.pure/validate` which has the following syntax:
```clojure
(taoclj.pure/validate params model messages)
```


###Examples:
```clojure
(require '[taoclj.pure :as pure])


(pure/validate {:id "3x"}
          	{:id {:type :int :required true}}
          	{:int/required ":name is required!"})

=> {:errors {:id "id is required!"}
    :params {:id "3x"}
    :values {:id nil}}



(pure/validate {:id "3"}
          	{:id {:type :int :required true}}
          	{:int/required ":name is required!"})

=> {:errors nil 
    :params {:id "3"} 
    :values {:id 3}} ;; the id has been parsed into an integer

```

s

###Validation Rules:
```clojure

{:type :string
 :required true  ;; param must not be nil or blank
 :required false ;; param may be nil or any value
 :length [2 nil] ;; must be 2 or more characters long
 :length [nil 5] ;; must be 5 or less characters long
 :length [2 5]   ;; must be between 2 and 5 characters long
 :custom my-fn } ;; a function that is passed the parameter & returns error string on failure, nil if ok.

{:type :int
 :required true  ;; param must parsable into integer
 :required false ;; param may be nil or any value
 :range [2 nil]  ;; must be 2 or greater
 :range [nil 5]  ;; must be 5 or less 
 :range [2 5] }  ;; must be between 2 and 5

{:type :email 
 :required true  ;; param must not be nil or blank, and valid format
 :required false ;; param may be nil/blank or if supplied a valid format 
 :custom my-fn } ;; a function that is passed the parameter & returns error string on failure, nil if ok.

```



###Messages:
```clojure
{:string/required ":name is required"
 :string/length-short ":min or more characters long"
 :string/length-long ":max or less characters long"
 
 :int/required ":name is required"
 :int/range-under ":min or more"
 :int/range-over ":max or less"
 :int/range "between :min :max"
 
 :email/required ":name is required"
 :email/invalid ":name must be valid email"}
```


## License

Copyright © 2012 Michael Ball

Distributed under the Eclipse Public License, the same as Clojure.
