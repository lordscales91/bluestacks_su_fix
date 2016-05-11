BlueStacks 2 Su Fixer
===
This simple app will *fix* the `su` binary installed by [SuperSU](https://play.google.com/store/apps/details?id=eu.chainfire.supersu) which becomes corrupted after reboot.

Actually, the fix is just removing the corrupted binary to let SuperSU install it again (Otherwise SuperSU will not be able to install it).

For Your Information: There is a way to get a **permanent root** on BlueStacks by using [Kingroot](https://kingroot.net/) (DO NOT confuse with [KingoROOT](https://www.kingoapp.com/)). However using SuperSU to get a **temporary root** has its advantages:

* Using a trusted app, probably the most used of its kind (NO You can't switch to SuperSU from Kingroot, that was possible before but not know)
* Be able to play some games that don't run if detect a rooted device ([Hide my root](https://play.google.com/store/apps/details?id=com.amphoras.hidemyroot) doesn't work, at least not for me)
* **Security:** You can leave the su binary on its **broken state** to prevent malicious apps from doing any harm to your precious virtual device.

Usage
---
Just install the app and run it. It will detect the status of your su binaries and provide you with a simple (and a bit ugly) interface.

There are only two buttons for now.
* "Fix SU" will remove the corrupted su and automatically call SuperSU after that
* "Unroot" will just remove the binary

Limitations
---
Currently the app relies on SuperSU to restore the binary, which means both "Fix SU" and "Unroot" are functionally the same. I have plans to implement a backup system to be able to restore the binary without the need of calling SuperSU