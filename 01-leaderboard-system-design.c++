#include <iostream>
#include <unordered_map>
#include <map>
#include <set>
#include <vector>

using namespace std;

class Leaderboard {
private:
    // Maps playerId to their current score
    unordered_map<int, int> scores;

    // Maps score to a set of playerIds with that score, sorted descending by score
    map<int, set<int>, greater<int>> rankTable;

public:
    // Add or update a player's score
    void addScore(int playerId, int score) {
        // If player already has a score, remove old entry from rankTable
        if (scores.count(playerId)) {
            int oldScore = scores[playerId];
            rankTable[oldScore].erase(playerId);
            // Clean up empty score buckets to keep rankTable concise
            if (rankTable[oldScore].empty()) {
                rankTable.erase(oldScore);
            }
        }
        // Update maps with new score
        scores[playerId] = score;
        rankTable[score].insert(playerId);
    }

    // Get the top N players as vector of pairs (playerId, score)
    vector<pair<int,int>> top(int N) {
        vector<pair<int,int>> result;
        for (auto& [score, playerSet] : rankTable) {
            for (int playerId : playerSet) {
                if ((int)result.size() == N) {
                    return result;
                }
                result.emplace_back(playerId, score);
            }
        }
        return result;
    }

    // Get the rank of a player (1-based), or -1 if player not found
    int getRank(int playerId) {
        if (!scores.count(playerId)) return -1;  // Player not found
        int rank = 1;
        int playerScore = scores[playerId];

        // Sum sizes of all score buckets with score strictly greater than player's score
        for (auto& [score, playerSet] : rankTable) {
            if (score == playerScore) break;
            rank += playerSet.size();
        }
        return rank;
    }
};

int main() {
    Leaderboard leaderboard;

    // Add some players and scores
    leaderboard.addScore(101, 1500);
    leaderboard.addScore(102, 1800);
    leaderboard.addScore(103, 1600);
    leaderboard.addScore(104, 1800); // Same score as player 102
    leaderboard.addScore(105, 1400);

    cout << "Top 3 players:" << endl;
    for (auto& [playerId, score] : leaderboard.top(3)) {
        cout << "Player " << playerId << " score: " << score << endl;
    }

    // Get ranks for some players
    cout << "\nRanks:" << endl;
    cout << "Player 102 rank: " << leaderboard.getRank(102) << endl;
    cout << "Player 101 rank: " << leaderboard.getRank(101) << endl;
    cout << "Player 105 rank: " << leaderboard.getRank(105) << endl;
    cout << "Player 999 rank (not present): " << leaderboard.getRank(999) << endl;

    // Update score to see rank change
    leaderboard.addScore(105, 1700);
    cout << "\nAfter updating Player 105's score to 1700:" << endl;
    cout << "Player 105 rank: " << leaderboard.getRank(105) << endl;

    cout << "\nTop 5 players:" << endl;
    for (auto& [playerId, score] : leaderboard.top(5)) {
        cout << "Player " << playerId << " score: " << score << endl;
    }

    return 0;
}
