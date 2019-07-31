Feature: articles routes

  Scenario: The route for get articles must response a 200
    When I send a "GET" request to "/articles"
    Then the response status code should be 200

  Scenario: The route for get article must response a 200
    When I send a "GET" request to "/articles/55a24426-139b-4ee7-b1e2-a3d016d66cc2"
    Then the response status code should be 200

  Scenario: The route for get article must response a 200
    When I send a "POST" request to "/articles" with body:
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
    When I send a "GET" request to "/articles/99afd1b1-3555-43c1-80a7-63c56e93d250"
    Then the response status code should be 200
    And the response should contain object:
      | id    | 99afd1b1-3555-43c1-80a7-63c56e93d250 |
      | title | title2                               |