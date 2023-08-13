# PicoBlaze Simulator for Android
This is my attempt to modify the [PicoBlaze Simulator in JavaScript](https://flatassembler.github.io/PicoBlaze/PicoBlaze.html), so that it can be run on Android without an Internet connection. This version is written in a combination of JavaScript and Java, and uses the V8 JavaScript engine built into Android. Right now, it is in very early stages of development.

<del>UPDATE on 10/08/2023: The development of this program has been falling behind the development of the PicoBlaze Simulator in JavaScript ever since the addition of the `display` preprocessor directive into PicoBlaze Simulator in JavaScript. Right now, the examples "*Decimal to Binary*" (the new version which includes `display`ing some messages) and "*Preprocessor Test*" do not assemble in PicoBlaze Simulator for Android.</del>

UPDATE on 13/08/2023: I have tried to make using the `display` preprocessor directive here a no-operation, rather than an error. However, I haven't tested that yet. 
