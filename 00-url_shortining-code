#include <iostream>
#include <unordered_map>
#include <string>
#include <random>

using namespace std;

class UrlShortener {
private:
    unordered_map<string, string> codeToUrl;
    unordered_map<string, string> urlToCode;
    const string BASE_HOST = "http://short.ly/";
    const string CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    const int CODE_LENGTH = 7;

    // Generate random code
    string generateCode() {
        static random_device rd;
        static mt19937 gen(rd());
        static uniform_int_distribution<> dis(0, CHARSET.size() - 1);

        string code;
        for (int i = 0; i < CODE_LENGTH; i++) {
            code += CHARSET[dis(gen)];
        }
        return code;
    }

public:
    // Shorten URL
    string shorten(const string& longUrl) {
        if (urlToCode.find(longUrl) != urlToCode.end()) {
            return BASE_HOST + urlToCode[longUrl];
        }
        string code = generateCode();
        while (codeToUrl.find(code) != codeToUrl.end()) { // Avoid collision
            code = generateCode();
        }
        codeToUrl[code] = longUrl;
        urlToCode[longUrl] = code;
        return BASE_HOST + code;
    }

    // Expand short URL
    string expand(const string& shortUrl) {
        string code = shortUrl.substr(BASE_HOST.size());
        if (codeToUrl.find(code) != codeToUrl.end()) {
            return codeToUrl[code];
        }
        return "";  // return empty string if not found
    }
};

// Example usage
int main() {
    UrlShortener shortener;

    string longUrl = "https://example.com";
    string shortUrl = shortener.shorten(longUrl);

    cout << "Short URL: " << shortUrl << endl;
    cout << "Expanded URL: " << shortener.expand(shortUrl) << endl;

    return 0;
}
