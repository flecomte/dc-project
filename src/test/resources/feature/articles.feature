Feature: articles routes

  Scenario: The route for get articles must response a 200
    When I send a GET request to "/articles"
    Then the response status code should be 200

  Scenario: The route for get one article must response a 200 and return article
    When I send a GET request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b"
    Then the response status code should be 200
    And the response should contain object:
      | id | 9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b |

  Scenario: The route for create article must response a 200 and return object
    Given I have citizen:
      | id        | 64b7b379-2298-43ec-b428-ba134930cabd |
      | firstName | Jaque                                |
      | lastName  | Dupuis                               |
    And I am authenticated as an user
    When I send a POST request to "/articles" with body:
    """
    {
      "version_id": "09c418b6-63ba-448b-b38b-502b41cd500e",
      "title": "title2",
      "annonymous": false,
      "content": "content2",
      "description": "description2",
      "tags": [
          "green"
      ],
      "created_by": {
          "id": "64b7b379-2298-43ec-b428-ba134930cabd"
      }
    }
    """
    Then the response status code should be 200
    And the response should contain object:
      | version_id | 09c418b6-63ba-448b-b38b-502b41cd500e |
      | title      | title2                               |