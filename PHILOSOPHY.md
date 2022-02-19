# Library Philosophy

This file is for answering questions around some of this library's design. This can also
serve as a reference point for why certain parts of the library are packaged a certain
way, or explaining choices made during creation, so that it can guide future decisions.

And if, down the line, a better approach is proposed, at least there's a paper trail for
the decisions already made so that more *educated* decisions can be made.


### What is the primary goal of this project?

To do Doom-related things at the command-line, mostly data-related, not game-related.


### [DECOHack] Why keep editor keys on Things as special comments instead of keywords in the parser?

Keeping familiarity between DECOHack and DECORATE.

Also, if identifier keywords were used instead, parsing would need to also be stricter, 
and since this is metadata-to-metadata translation, the key support is intentionally 
dumb and only passed along to the exporter for silent verification.


### [DECOHack] Why can't I use "GOTO Label+Offset" like DECORATE?

While it may be possible to one day add support for this sort of thing, a few goals around
DECOHack's design would be compromised if implemented, despite it being based on DECORATE.

First, since DECOHack is able to change/overwrite existing states and also remap how
states link to the next, any use of offsetting would be misleading - is the offset amount 
in *redefined* states, or an offset from an existing label's current index? Also, while 
DECORATE supported this sort of thing, it eventually became (G)ZDoom's entire actor 
definition language, and so the concept of "existing states" is not a thing in (G)ZDoom, 
which needs to be accounted for in DECOHack. The "goto" keyword, in DECOHack's case, is far 
more flexible in this regard when compared to DECORATE, since you can point to any available
state defined by Doom.

Of course, needing to handle existing states isn't an issue for EXTENDED or MBF21 patches, 
given the hundreds of new available states to fill, but it is very much an issue for 
Vanilla/Boom modding, which DECOHack still needs to support, and why it exists in the
first place.

Second, I (MTrop) **never** liked the offset mechanic, especially when *state labels* 
offer a far more readable alternative rather than searching for that label and then 
*counting each state (and multiple frames)* to find exactly where it jumps to. Besides,
ever since custom labels were added to DECORATE (a long, long, time ago), offsets became
entirely obsolete, from a design standpoint.

There's nothing wrong in forcing users to write more readable code!


### [DECOHack] The parser will "match" tokens, and other times merely checks token types before manually advancing. Why?

Error message manipulation. *Whenever possible,* it's better to point out to the user 
the exact location of where the problem lies, and not after. The parser ingests tokens in
a way that facilitates this, most of the time. If you modify the parser, err on the side of
what makes an informative, convey-able error!


### [DECOHack] Why add Thing/Weapon aliases as a feature? Isn't #define good enough?

Very good question! 

Defines are lexer-wide, and are replaced at the token level before DECOHack has a chance to 
read them. Aliases are DECOHack-centric, and are contextual to Thing and Weapon clauses and 
definitions. You are better off using aliases or the pre-packaged defines via the patch 
type #includes.


### [DECOHack] Why aren't DECOHack's base patches built from a source document a la DECORATE/ZScript?

The main reason is - Doom's executables and other data are laid out in a very specific way,
and if I attempt to rebuild that initial actor data using DECOHack's state-filling logic, 
there's a good chance that the result will not line up with the original, which will make 
patches with redundant "changes," or worse, ruin hardcodings that the author did not intend 
to change, out-of-the-box. These problems compound if I (or anybody else) have to do other 
internal changes.

I could hardcode in specific states, but those would become less readable (or useful as examples) 
if I have to keep defining specific states to have specific data.

For the time being, you are better off using the [ZDoom Wiki](https://zdoom.org/wiki/Classes) pages
as guidance.


### [WadScript] Why is there no WADREPLACE() built-in for replacing WAD entries?

"Replacing" an entry is essentially removing and adding at the same index, but you can
remove an entry two ways: Deletion of just the entry, or deletion of the entry plus
data purge (and reshuffling of other entry offsets).

Since the user knows the end result of the WAD data manipulation, it's better to allow 
them to make a decision based around a potentially expensive set of calls.

The `Wad.replaceEntry()` method in the Doom library is a tad more specialized 
for its expected use case, but this approach is the more correct one in terms of user 
control and WadScript's use case.
