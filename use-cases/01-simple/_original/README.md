# Board Game Buddy

The example application that is built in the book Spring AI in Action. This
folder contains the code from chapter 1.

## Quick Start
You'll need an OpenAI API key. You can get one by signing up at
https://platform.openai.com/ and creating an API key. Set it to an environment
variable named `OPENAI_API_KEY` before running the application.

Run the following command to start the application:

```shell
./gradlew bootRun
```

Then try it out by submitting a request to the API exposed by the application.
Using `curl`:

```shell
curl localhost:8080/ask -H"Content-type: application/json" \
     -d'{"question":"Why is the sky blue?"}'
```

Or using HTTPie:

```shell
http :8080/ask question="Why is the sky blue?"
```
