# Pure

Pure is a validation library for clojure. 

One of the most tedious parts of building web applications is validating & parsing string based parameters into the values your application needs. Pure uses the type of your model attribute, as well as validation rules to do this automatically.

Pure is currently very incomplete with few rules implemented but is under active development.




## Installation

Add the following dependency to your `project.clj` file:

```clojure
[pure "0.0.1"]
```


## Usage


The main validation function is `pure.core/validate` which has the following syntax:
```clojure
(validate map-of-params
          map-of-model
          list-of-allowed-fields
          map-of-messages)
```


###Example:
```clojure
(require '[pure.core :as pure])

;; invalid id parameter
(pure/validate {:id "3x" :unwanted "abc"}
          	   {:id {:type :int :required true}}
          	   [:id]
          	   {:int/required ":name is required!"})
	
=> {:errors {:id "id is required!"} :params {:unwanted "abc", :id "3x"} :values {:id nil}}


;; valid id parameter
(pure/validate {:id "3"}
          	   {:id {:type :int :required true}}
          	   [:id]
          	   {:int/required ":name is required!"})

=> {:errors {} :params {:unwanted "abc" :id "3"} :values {:id 3}}

```

Note the following about the above example:

 - For valid parameter sets, the errors map will be empty.
 - The original values in the supplied params are added to result because the often are needed for redisplay on validation failure.
 - The error message is formatted with the name of the parameter.
 - Only allowed parameters are passed on as values.
 - Most importantly _the integer has been automatically parsed for you_ and placed in the values map.



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
