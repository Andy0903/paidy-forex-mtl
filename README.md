# Paidy Take-Home Coding Exercise FOREX-MTL

## Instructions

See the given instructions [here](Forex.md).

## Running the program

To run the program two things are necessary.
1. Start the one-frame docker given by Paidy (see [instructions](#instructions))
2. Run the program locally with the command
> sbt run

Then you can request a rate from the program

> curl 'localhost:5005/rates?from=AUD&to=SGD'

The URL can be changed in the application.conf, and the supported currencies can be seen in Currency.scala.

## Assumptions and limitations

Here I will list assumptions I made during the implementation.

1. Currency pairs are not always inverses of each other, i.e. we can not assume AUDSGD == 1 / SGDAUD.
This kind of currency treatment can be seen in Currency.scala when getting all possible currency pairs.
2. If OneFrame is slow at responding, many calls can be made to the API in a short amount of time from the program if many connections request it. A possible solution to this limitation would be to use a scheduled job that tries to get the rates from OneFrame at a fixed interval instead. In that way it would not be connected to the amount of calls our program recieves itself, hence safer.
3. I've assumed that a possible latency between request and response is acceptable. Once again, if OneFrame is slow at responding the program will also be slow at responding in the case that the cache is empty or has expired values. This could (like in point 2) be solved by having a scheduler instead as the requests to OneFrame would not happen in the program flow that the user experiences when making a request. 

As can be seen in both point 1 and 2, I lean towards a scheduler being a more robust solution. However, with the instructions emphasizing to keep the solution simple I've decided to do that and instead hope that the discussion part will show my understanding of different ways to solve the problem and their perks.

## Possible improvements

1. Automated tests
2. Maybe return the last retrieved rate rather than nothing at all in case communication with OneFrame can not be established anymore (with some warning that they are older than the limit allows).
3. Logging for debug purposes
4. Possibly use a scheduler solutions instead to have a more robust program depending on the use cases and reliability of OneFrame responses.