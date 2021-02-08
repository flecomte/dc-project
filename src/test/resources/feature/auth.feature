@auth
Feature: Auth routes

  Scenario: I can resiter
    When I send a POST request to "/register" with body:
    """
    {
      "name": {"first_name":"George", "last_name":"MICHEL"},
      "birthday": "2001-01-01",
      "user":{
        "username": "george-junior",
        "password": "azerty"
      },
      "email": "george-junior@gmail.com"
    }
    """
    Then the response status code should be 200
    And the Response should contain:
    """
    eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.
    """

  Scenario: I cannot create user if no username was sent
    When I send a POST request to "/register" with body:
    """
    {
      "name": {"first_name":"George2", "last_name":"MICHEL2"},
      "birthday": "2001-01-01",
      "user":{
        "username": "",
        "password": ""
      }
    }
    """
    Then the response status code should be 400

  Scenario: I can login with username and password
    The route for create citizen must response a 200 and return object
    Given I have citizen Niels Bohr
    When I send a POST request to "/login" with body:
    """
    {
      "username": "niels-bohr",
      "password": "azerty"
    }
    """
    Then the response status code should be 200
    And the Response should contain:
    """
    eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.
    """