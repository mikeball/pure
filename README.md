# Pure

Pure is a validation library for clojure. It lets you specify a model and a set of validation message formats, then it will validate the params returning error messages and parsed values to use.



## Installation

Add the following dependency to your `project.clj` file:

    [pure "0.0.1"]



## Usage

Basic example:

    (require '[pure.core :as pure])

    (def messages {:string/required ":name is required" 
                   :int/required ":name must be a number"})
    (def model {:first-name {:type :string :required true} 
                :age {:type :int :required true}})
    (def allowed [:first-name :age])
    

    (pure/validate {:first-name "" :age ""} model allowed messages)
    => {:errors {:age "age must be a number", :first-name "first-name is required"}
        :values {:age nil, :first-name ""}
        :params {:age "", :first-name ""}}


    (pure/validate {:first-name "John" :age "1"} model allowed messages)
    => {:errors {}
        :values {:age 1, :first-name "John"}
        :params {:age "1", :first-name "John"}}



## License

Copyright © 2012 Michael Ball

Distributed under the Eclipse Public License, the same as Clojure.
