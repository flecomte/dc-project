@citizen
Feature: citizens routes

  Scenario: The route for get citizens must response a 200
    Given I have citizen Jean Perrin with ID "5267a5c6-af42-4a02-aa2b-6b71d2e43973"
    And I am authenticated as Jean Perrin
    When I send a GET request to "/citizens"
    Then the response status code should be 200

  Scenario: The route for get one citizen must response a 200 and return citizen
    Given I have citizen Linus Pauling with ID "47a05c0f-7329-46c3-a7d0-325db37e9114"
    Given I am authenticated as Linus Pauling
    When I send a GET request to "/citizens/47a05c0f-7329-46c3-a7d0-325db37e9114"
    Then the response status code should be 200
    And the response should contain object:
      | id | 47a05c0f-7329-46c3-a7d0-325db37e9114 |

  Scenario: Can get connected citizen
    Given I have citizen Henri Becquerel with ID "47356809-c8ef-4649-8b99-1c5cb9886d38"
    And I am authenticated as Henri Becquerel
    When I send a GET request to "/citizens/current"
    Then the response status code should be 200
    And the response should contain object:
      | id | 47356809-c8ef-4649-8b99-1c5cb9886d38 |

  @online
  Scenario: Can be connect with Passwordless auth
    Given I have citizen
      | id        | c606110c-ff0e-4d09-a79e-74632d7bf7bd |
      | email     | fabrice.lecomte.be@gmail.com         |
      | firstName | Leonhard                             |
      | lastName  | Euler                                |
    When I send a POST request to "/auth/passwordless" with body:
    """
    {
      "url": "https://dc-project.fr/password/reset",
      "email": "fabrice.lecomte.be@gmail.com"
    }
    """
    Then the response status code should be 204

  Scenario: Can be change my password
    Given I have citizen Georges Charpak with ID "0c966522-4071-43e5-a3ca-cfff2557f2cf"
    And I am authenticated as Georges Charpak
    When I send a PUT request to "/citizens/0c966522-4071-43e5-a3ca-cfff2557f2cf/password/change" with body:
    """
    {
      "old_password": "azerty",
      "new_password": "qwerty"
    }
    """
    Then the response status code should be 201

  Scenario: If a send bad request when a change password, that return a 400 Bad request

    Given I have citizen Louis Breguet with ID "6cf2a19d-d15d-4ee5-b2a9-907afd26b525"
    And I am authenticated as Louis Breguet
    When I send a PUT request to "/citizens/6cf2a19d-d15d-4ee5-b2a9-907afd26b525/password/change" with body:
    """
    {
      "plup": "azerty",
      "gloup": "qwerty"
    }
    """
    Then the response status code should be 400
