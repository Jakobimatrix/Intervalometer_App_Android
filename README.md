# Intervalometer_App_Android
An App for Android to program the instructions for the intervallometer (see repository Intervalometer_Hardware)

### Features so far:
 * Connect Bluetooth module to mobile device and transfere static function commands (once).
 * Construct Function Commands via gui featuring:
   * Constant Function (Every Intervallometer does this)
   * Linear Function (Change the trigger intervalls linear over time)
   * Quadratic Function (left- and right-sided extrema)
   * Sigmodid Function for smoother transitions
 * Name, save, copy and delete Function Command Templates
 
## Further Ideas/Todos
 - [ ] Change intervallometer instructions online (while running) to react to situations
    * E.g. start with constant interval; when requested smoothly change interal over N frames to new constant target interval.
 - [ ] Preview of the result (in a simulation (rendered movie), where the user can choose the scene)
 - [ ] Show plot of frame density over time like |  |  |  | | | |||| | |  | with a timeline




