# Intervalometer_App_Android
An App for Android to program the instructions for the intervalometer (see repository Intervalometer_Hardware)
Make sure to check out equal TAGs. Commits without (or different) TAGs might not work 
together.

### Features so far:
 * Connect Bluetooth module to mobile device and transfere static function commands (once).
 * Construct Function Commands via gui featuring:
   * Constant Function (Every Intervalometer does this)
   * Linear Function (Change the trigger intervals linear over time)
   * Quadratic Function (left- and right-sided extrema)
   * Sigmodid Function for smoother transitions
 * Name, save, copy and delete Function Command Templates
 
![Preview](https://github.com/Jakobimatrix/Intervalometer_App_Android/tree/master/impressions/preview.gif)
 
## Further Ideas/Todos
 - [ ] Change intervalometer instructions online (while running) to react to situations
    * E.g. start with constant interval; when requested smoothly change interal over N frames to new constant target interval.
 - [ ] Preview of the result (in a simulation (rendered movie), where the user can choose the scene)
 - [ ] Show plot of frame density over time like |  |  |  | | | |||| | |  | with a timeline
 - [ ] Show Label for Y-Axis and Labels (x,y) for selected Manipulators
 - [ ] Before transmitting check if calculated coefficients resemble user input (not true 
for very gentle slops (of Quadratic functons). IF default float precision is not enough, 
switch to the "Precision protocol"
 - [ ] implement precision protocol: each constant has its own encoded precision divider


