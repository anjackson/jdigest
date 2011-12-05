JDigest
=======

A fork of the JDigest application from http://code.google.com/p/jdigest/

The idea is to strip this down further, generating manifests in a canonical format:

* Use SHA-256 only.
* Use UTF-8 only.
* Use UNIX-style relative pathnames (i.e. forward slashes for the directory separator).
* Use binary-mode and indicate this in the standard way, with an asterisk.
* Use a file name 'manifest.sha256' for the manifest, by default.
