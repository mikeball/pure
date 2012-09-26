# Pure

Pure is a validation library for clojure. 

One of the most tedious parts of building web applications is validating & parsing string based parameters into the values your application needs. Pure uses the type of your model attribute, as well as validation rules to do this automatically.

Pure is currently very incomplete with few rules implemented but is under active development.




## Installation

Add the following dependency to your `project.clj` file:

    [pure "0.0.1"]



## Usage


The main validation function is `pure.core/validate` which has the following syntax:

    (validate map-of-params 
              map-of-model 
              list-of-allowed-fields 
              map-of-messages)



###Example:

    (require '[pure.core :as pure])

	;; invalid id parameter
	(pure/validate {:id "3x" :unwanted "abc"}
          		   {:id {:type :int :required true}}
          		   [:id]
          		   {:int/required ":name is required!"})
	
	=> {:errors {:id "id is required!"}
	    :params {:unwanted "abc", :id "3x"}
	    :values {:id nil}}



	;; valid id parameter
	(pure/validate {:id "3"}
          		   {:id {:type :int :required true}}
          		   [:id]
          		   {:int/required ":name is required!"})

	=> {:errors {}, :params {:unwanted "abc" :id "3"} :values {:id 3}}


Note the following about the above example:

 - For valid parameter sets, the errors map will be empty.
 - The original values in the supplied params are added to result because the often are needed for redisplay on validation failure.
 - The error message is formatted with the name of the parameter.
 - Only allowed parameters are passed on as values.
 - Most importantly ** the integer has been automatically parsed for you** and placed in the values map.



###Validation Rules:

    ;; string rules
	{:type :string ;; type must be supplied
	 
	 :required true ;; param must not be nil or blank
	 :required false ;; param may be nil or any value
	 
	 :length [2 nil] ;; must be at least 2 characters long
	 :length [nil 5] ;; must be less than 5 characters long
	 :length [2 5] ;; must be between 2 and 5 characters long  
	}


	;; int rules
	{:type :int ;; type must be supplied
	 
	 :required true ;; param must parsable into integer
	 :required false ;; param may be nil or any value
	}




###Messages:

	{:string/required ":name is required"
     :string/length-short ":name must be at least :min characters long"
     :string/length-long ":name must be less than :max characters long"
     
     :int/required "please enter a valid number"
     :int/range ":name must be a number between :min and :max"
     }



## License

Copyright © 2012 Michael Ball

Distributed under the Eclipse Public License, the same as Clojure.
