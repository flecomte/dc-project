@constitution
Feature: constitution

  Scenario: The route for get constitutions must response a 200
    Given I have 3 constitution
    When I send a GET request to "/constitutions"
    Then the response status code should be 200

  Scenario: The route for get one constitution must response a 200 and return constitution
    Given I have constitution with ID "0321c8d1-4ce3-4763-b5f4-a92611d280b4"
    When I send a GET request to "/constitutions/0321c8d1-4ce3-4763-b5f4-a92611d280b4"
    Then the response status code should be 200
    And the response should contain object:
      | id | 0321c8d1-4ce3-4763-b5f4-a92611d280b4 |

  Scenario: The route for create constitution must response a 200 and return object
    Given I have citizen Henri Poincaré
    And I am authenticated as Henri Poincaré
    When I send a POST request to "/constitutions" with body:
    """
    {
       "version_id":"15814bb6-8d90-4c6a-a456-c3939a8ec75e",
       "title":"Hello world!",
       "annonymous":true,
       "titles":[
          {
             "name":"plop",
             "rank":0
          }
       ]
    }
    """
    Then the response status code should be 200
    And the response should contain object:
      | version_id | 15814bb6-8d90-4c6a-a456-c3939a8ec75e |
      | title      | Hello world!                         |