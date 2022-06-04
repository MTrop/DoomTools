# Contributing

To contribute to this project, you must follow the Code of Conduct and Coding Standards, as well as observe the guidelines for Pull Requests.

### Code of Conduct

1. Keep your criticism constructive. Discussion over algorithms, implementations, and documentation/comment wording is constructive. Personal attacks are **not constructive.**

2. You may not harass other contributors in any way, nor other contributors of other projects, nor do anything that would promote a culture of hostility to prevent others from contributing. Contributing to this project makes you a tangential representative of it. Act accordingly. 


### Coding Standards

1. The bracing style is [Allman](https://en.wikipedia.org/wiki/Indentation_style#Allman_style), for the most part. Some try/catches use [K&R](https://en.wikipedia.org/wiki/Indentation_style#K&R_style). The primary maintainer of this codebase prefers consistency within code over arguing the merits of each style. Discussions over a change of style will not be entertained.

2. Hard tabs in code. Again, the primary maintainer of this codebase prefers consistency within code over arguing the merits of each style. Discussions over a change of style will not be entertained.

3. While this is intended to be an end-user utility package, all non-getter/setter **public** and **protected** fields and methods **must** have Javadoc comments. Private or default method/field Javadocs are optional (but may be encouraged if the method is very complex), but sometimes just a comment is fine, for elucidation. 

4. Unless the variable is temporary or "throwaway," use descriptive names. We have IDEs, now. Autocomplete is a thing. 

5. Methods must have purposeful names, but not excessively large, descriptive ones. That's what documentation is for.

6. You may not add additional dependencies to this project without adequate justification or review. Stay in the [java.desktop](https://docs.oracle.com/en/java/javase/11/docs/api/java.desktop/module-summary.html) module (and its child dependencies). *Removal* of dependencies, if any, are always a welcome proposition.

### Pull Requests

1. All Pull Requests are subject to review before acceptance. If your feature or contribution is not accepted, understand that it may be due to it not aligning with the project's philosophy or purpose, coding standards aside.

2. All Pull Requests must be made against `master` or a "master-derivative" branch.

3. You must specify what you are adding or fixing. Without context for the PR, it cannot be reviewed adequately, nor ultimately accepted.

4. Understand that not all contributions are worthy by default - any new features must fill a utilitarian deficit or fix a clear bug ("bug" is this case is defined as an unintended behavior). 

5. Refactorings of existing classes/packages are at the discretion of the primary maintainer. Please ask if you are unsure whether or not a feature requires a new package, and accept direction if another or different package (or project) is necessary to accommodate your feature.

6. "Bikeshed" features/corrections will not be accepted without adequate justification.

7. The primary maintainer of this codebase is **not** a believer in "Good or readable code needs no documentation." If you are asked to clarify sections of code with plain English, do so or you will risk the rejection of your request.

8. Please amend the CHANGELOG - add a "Changed in [NOW]" section if it doesn't exist.

9. Your code must be compatible with the license of this project, and be written with the understanding that it will be published under this project's license.  

