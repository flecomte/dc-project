Feature: comment Article

  Scenario: Can comment an article
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    And I have article with id "9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b"
    When I send a POST request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/comments" with body:
    """
    {
      "content": "Hello mister"
    }
    """
    Then the response status code should be 201

  Scenario: Can get all comment on article
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    And I have article with id "9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b"
    When I send a GET request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/comments"
    Then the response status code should be 200

  Scenario: Can get all comment on article sorted by votes
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    And I have article with id "9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b"
    When I send a GET request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/comments?sort=votes"
    Then the response status code should be 200
    And the response should contain object:
      | result[1].votes.up | 1 |

  Scenario: Can get comments on articles of the current citizen
    Given I have citizen John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    And I have article with id "9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b"
    When I send a GET request to "/citizens/64b7b379-2298-43ec-b428-ba134930cabd/comments/articles"
    Then the response status code should be 200
    And the response should contain object:
      | current_page | 1  |
      | limit        | 50 |

  Scenario: Can edit a comment
    Given I am authenticated as username 3 with id "92877af7-0a45-fd6a-2ed7-fe81e1236b78"
    When I send a PUT request to "/comments/2f01c257-cf20-3466-fb10-a3b8eff12a97" with body:
    """
    Hello boy
    """
    Then the response status code should be 200
    And the JSON should contain:
      | content | Hello boy |

  Scenario: Can get comment by its ID
    When I send a GET request to "/comments/2f01c257-cf20-3466-fb10-a3b8eff12a97"
    Then the response status code should be 200
    And the JSON should contain:
      | content | Hello boy |
