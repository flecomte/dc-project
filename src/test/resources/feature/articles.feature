@article
@disable
Feature: articles routes

  Scenario: The route for get articles must response a 200
    Given I have 3 article
    When I send a GET request to "/articles"
    Then the response status code should be 200

  Scenario: Can get articles filtered by workgroup
    Given I have 3 article
    And I have workgroup:
      | id   | 2bccd5a7-9082-4b31-88f8-e25d70b22b12 |
      | name | Les papy                             |
    And I have article created by workgroup ID "2bccd5a7-9082-4b31-88f8-e25d70b22b12"
    When I send a GET request to "/articles?workgroup=2bccd5a7-9082-4b31-88f8-e25d70b22b12"
    Then the response status code should be 200
    And the JSON should contain:
      | total                   | 1        |
      | result[0]workgroup.name | Les papy |

  Scenario: Can get versions of article by the id
    Given I have article
      | id | 13e6091c-8fed-4600-b079-a97a6b7a9800 |
    When I send a GET request to "/articles/13e6091c-8fed-4600-b079-a97a6b7a9800/versions"
    Then the response status code should be 200

  Scenario: The route for get one article must response a 200 and return article
    Given I have article
      | id | 65cda9f3-8991-4420-8d41-1da9da72c9bb |
    When I send a GET request to "/articles/65cda9f3-8991-4420-8d41-1da9da72c9bb"
    Then the response status code should be 200
    And the response should contain object:
      | id | 65cda9f3-8991-4420-8d41-1da9da72c9bb |

  Scenario: The route for create article must response a 200 and return object
    Given I have citizen John Doe
    And I am authenticated as John Doe
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
      ]
    }
    """
    Then the response status code should be 200
    And the response should contain object:
      | version_id | 09c418b6-63ba-448b-b38b-502b41cd500e |
      | title      | title2                               |