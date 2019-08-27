Feature: comment Article and Constitution

  # Article
  Scenario: The route for comment article must response a 201 and return object
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    And I have article with id "9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b"
    When I send a POST request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/comments" with body:
    """
    Hello mister
    """
    Then the response status code should be 201

  Scenario: The route for get comments of articles must response a 200 and return objects
    Given I have citizen John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    And I have article with id "9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b"
    When I send a GET request to "/citizens/64b7b379-2298-43ec-b428-ba134930cabd/comments/articles"
    Then the response status code should be 200
    And the response should contain object:
      | current_page | 1  |
      | limit        | 50 |

  Scenario: The route for edit comment must response a 200 and return object
    Given I am authenticated as username 3 with id "92877af7-0a45-fd6a-2ed7-fe81e1236b78"
    When I send a PUT request to "/comments/2f01c257-cf20-3466-fb10-a3b8eff12a97" with body:
    """
    Hello boy
    """
    Then the response status code should be 200
    # TODO check if data is realy edited
  And the JSON should contain:
    | content | Hello boy |


  # Constitution
  # TODO
