Create Action
============

* [ ] Create [OpenApi](../src/main/resources/openapi.yaml) documentation
* [ ] Create route
    * [ ] Create request with [Location](https://ktor.io/docs/features-locations.html)
        * [ ] Create Validation of request with [Konform](https://www.konform.io)
            * [ ] Test validation
    * [ ] [Check auth](../src/main/kotlin/fr/dcproject/component/auth/CitizenContext.kt) on protected route
        * [ ] [Create test for auth](../src/test/kotlin/integration/steps/given/Auth.kt)
    * [ ] Return must not be an Entity
    * [ ] Tests request:
        * [ ] Route with these params
        * [ ] Body of the request
          * [ ] Success
          * [ ] BadRequest
        * [ ] Body and request params must [match with the openapi schema](../src/test/kotlin/integration/steps/then/schema.kt)
* [ ] Create [AccessControl](../src/main/kotlin/fr/dcproject/common/security/AccessControlModule.kt)
    * [ ] Test [AccessControl](../src/test/kotlin/integration/steps/given/Auth.kt)
  

* [ ] Create Entity
  

* [ ] Create Repository
* [ ] Create SQL function in file
    * [ ] Create Tests SQL
  
* [ ] Tests
    * [ ] Test BadRequest