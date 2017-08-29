# Keystroke dynamics as an additional authentication factor

This project is used for analysis of keystroke dynamics as an additional authentication factor.

Keystroke dynamics is a person's unique typing behavior. The way how user types may identify him with a high probability.
Most popular features used in keystroke dynamics analysis are:
1.  Hold time (time interval of pressing the key, i.e. from press to release)
2.  Time between keys
    * Release-press time (time between releasing one key and pressing the next one)
    * Press-press time (time between pressing one key and pressing the next one)
3.  Error rate
4.  Other statistical characteristics (e.g. mean time of pressing a key, deviation, etc.)


The benefits of this type of behavioral biometrics:
- no additional hardware is required except of keyboard
- user does not have to do any additional actions

Drawbacks of keystroke dynamics are:
- accuracy is significantly lower than using physiological characteristics like fingerprints and others
- user template is tied with specific keyboard, changing it may significantly reduce detection accuracy
- our typing rhythm may change based on a lot of conditions
- if used for authentication, it is difficult to achieve appropriate accuracy with a little number of samples

# Available modes

Keystroke dynamics biometric system can work in one of two modes:
* identification
* verification

Identification takes input sample of extracted features and determines which user most likely sent it or unknown user at all.
So, identification requires samples of all users in the system to train classifier. It's resource intensive.
Training classifier for many users may take a lot of time and it's not guaranteed that its performance will be good enough.
Identification is useful mostly for continuous authentication.

Verification takes input sample of extracted features and determines whether it is similar enough
to stored biometric template of requested user.

Best experiment results during this project have been achieved using verification mode.

# Performance results

These results have been achieved using verification by Manhattan scaled distance
on dataset and technique from http://www.cs.cmu.edu/~keystroke/

![Performance](https://user-images.githubusercontent.com/6876210/29736103-09a259d0-8a07-11e7-8f90-147e300c2b48.png)