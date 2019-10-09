Feature: citizens routes

  Scenario: The route for get citizens must response a 200
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a GET request to "/citizens"
    Then the response status code should be 200

  Scenario: The route for get one citizen must response a 200 and return citizen
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a GET request to "/citizens/6434f4f9-f570-f22a-c134-8668350651ff"
    Then the response status code should be 200
    And the response should contain object:
      | id | 6434f4f9-f570-f22a-c134-8668350651ff |

  Scenario: Can get connected citizen
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a GET request to "/citizens/current"
    Then the response status code should be 200
    And the response should contain object:
      | id | 64b7b379-2298-43ec-b428-ba134930cabd |

  Scenario: Can be connect with SSO
    Given I have citizen:
      | id        | c606110c-ff0e-4d09-a79e-74632d7bf7bd |
      | firstName | John                                 |
      | lastName  | Doe                                  |
      | email     | fabrice.lecomte.be@gmail.com         |
    When I send a POST request to "/sso" with body:
    """
    {
      "url": "https://dc-project.fr/password/reset",
      "email": "fabrice.lecomte.be@gmail.com"
    }
    """
    Then the response status code should be 204

  Scenario: Can be change my password
    Given I am authenticated as Joe Patate with id "c211dca6-aa21-45c2-95ba-c7f2179ee37e"
    When I send a PUT request to "/citizens/c211dca6-aa21-45c2-95ba-c7f2179ee37e/password/change" with body:
    """
    {
      "password": "qwerty"
    }
    """
    Then the response status code should be 201
