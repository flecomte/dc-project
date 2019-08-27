Feature: Auth routes

  Scenario: The route for create citizen must response a 200 and return object
    When I send a POST request to "/register" with body:
    """
    {
      "name": {"first_name":"George", "last_name":"MICHEL"},
      "birthday": "2001-01-01",
      "user":{
        "username": "george-junior",
        "plain_password": "azerty"
      }
    }
    """
    Then the response status code should be 200
    And the Response should contain:
    """
    eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.
    """

  Scenario: The route for create citizen must response a 200 and return object
    When I send a POST request to "/register" with body:
    """
    {
      "name": {"first_name":"George2", "last_name":"MICHEL2"},
      "birthday": "2001-01-01",
      "user":{
        "username": "",
        "plain_password": ""
      }
    }
    """
    Then the response status code should be 400

  Scenario: The route for create citizen must response a 200 and return object
    When I send a POST request to "/login" with body:
    """
    {
      "name": "username-1",
      "password": "azerty"
    }
    """
    Then the response status code should be 200
    And the Response should contain:
    """
    eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.
    """