Feature: constitution routes

  Scenario: The route for get constitutions must response a 200
    When I send a GET request to "/constitutions"
    Then the response status code should be 200

  Scenario: The route for get one constitution must response a 200 and return constitution
    When I send a GET request to "/constitutions/0ca489a6-ef68-8bd5-2355-5793d4b3d66c"
    Then the response status code should be 200
    And the response should contain object:
      | id | 0ca489a6-ef68-8bd5-2355-5793d4b3d66c |

  Scenario: The route for create constitution must response a 200 and return object
    Given I have citizen John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a POST request to "/constitutions" with body:
    """
    {
       "version_id":"15814bb6-8d90-4c6a-a456-c3939a8ec75e",
       "title":"Hello world!",
       "annonymous":true,
       "titles":[
          {
             "id":"8156b66f-a9c8-4fd9-8375-a8a1f42ccfd2",
             "name":"plop",
             "rank":0,
             "created_by":{
                "id":"64b7b379-2298-43ec-b428-ba134930cabd"
             }
          }
       ],
       "created_by":{
          "id":"64b7b379-2298-43ec-b428-ba134930cabd"
       },
       "created_at":null,
       "version_id":"3311a7af-2a62-4e31-b4cd-889f8ead9737",
       "version_number":null
    }
    """
    Then the response status code should be 200
    And the response should contain object:
      | version_id | 15814bb6-8d90-4c6a-a456-c3939a8ec75e |
      | title      | Hello world!                         |