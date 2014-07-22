#include <Servo.h>
#include <adk.h>

#define DIR_A 12
#define BRAKE_A 9
#define SPEED_A 3

#define DIR_B 13
#define BRAKE_B 8
#define SPEED_B 11

#define RCVSIZE 128

// Accessory descriptor. It's how Arduino identifies itself in Android.
char accessoryName[] = "UDOO_robot";
char manufacturer[] = "AidiLab";
char model[] = "UDOOdroidconADK";
char versionNumber[] = "1.0";
char serialNumber[] = "1";
char url[] = "http://www.udoo.org";

USBHost Usb;
ADK adk(&Usb, manufacturer, model, accessoryName, versionNumber, url, serialNumber);
uint8_t buffer[RCVSIZE];
uint32_t bytesRead = 0;

int mspeed = 130;

Servo rightArm;
Servo leftArm;
const int rightArmPin = 5;
const int leftArmPin  = 6;

const int armRUp   = 165;
const int armRFlex = 60;
const int armRDown = 15;
const int armLUp   = 15;
const int armLFlex = 120;
const int armLDown = 165;

// command from Android	
const int FORWARD_COMMAND = 0;
const int BACK_COMMAND    = 1;
const int RIGHT_COMMAND   = 2;
const int LEFT_COMMAND    = 3;
const int GOODBOY_COMMAND = 4;
const int BADBOY_COMMAND  = 5;
const int CUTEBOY_COMMAND = 6;
const int HELLO_COMMAND   = 7;
const int MOONWALK_COMMAND= 8;

void setup() {
    Serial.begin(115200);
    Serial.println("Ready to listen!");
    rightArm.attach(rightArmPin);
    leftArm.attach(leftArmPin);
    rightArm.write(armRDown);
    leftArm.write(armLDown);
    pinMode(DIR_A, OUTPUT);
    pinMode(BRAKE_A, OUTPUT);
    pinMode(SPEED_A, OUTPUT);
    pinMode(DIR_B, OUTPUT);
    pinMode(BRAKE_B, OUTPUT);
    pinMode(SPEED_B, OUTPUT);

    stopEngine();
    
    delay(500);
}

void loop() {
  
  Usb.Task();

  if (adk.isReady()){
     adk.read(&bytesRead, RCVSIZE, buffer);
     if (bytesRead > 0){
        Serial.print("received command: ");
        Serial.println(extractCommand(buffer[0]));
        switch (extractCommand(buffer[0])) {
          case FORWARD_COMMAND:
            goForward(mspeed);
            break;
          case BACK_COMMAND:
            goBackward(mspeed);
            break;
          case RIGHT_COMMAND:
            turnRight(mspeed);
            break;
          case LEFT_COMMAND:
            turnLeft(mspeed);
            break;
          case GOODBOY_COMMAND:
            goodCase();
            break;
          case BADBOY_COMMAND:
            badCase();
            break;
          case CUTEBOY_COMMAND:
            cuteCase();
            break;
          case HELLO_COMMAND:
            helloCase();
            break; 
          case MOONWALK_COMMAND:
            moonWalkCase();
            break; 
          default:
            break;
        }
     }
  } 
  delay(30);
}

uint8_t extractCommand(uint8_t received) {
  return received - 48;
}

// Main movement command
void goForward(int mspeed) {  
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, HIGH);
  digitalWrite(DIR_B, HIGH);
  analogWrite(SPEED_A, mspeed);
  analogWrite(SPEED_B, mspeed);
  delay(500);
  stopEngine();
}

void goBackward(int mspeed) {  
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, LOW);
  digitalWrite(DIR_B, LOW);
  analogWrite(SPEED_A, mspeed);
  analogWrite(SPEED_B, mspeed);
  delay(300);
  stopEngine();
}

void turnRight(int mspeed) {  
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, LOW);
  digitalWrite(DIR_B, HIGH);
  analogWrite(SPEED_A, mspeed);
  analogWrite(SPEED_B, mspeed);
  delay(600);
  stopEngine();
}

void turnLeft(int mspeed) {
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, HIGH);
  digitalWrite(DIR_B, LOW);
  analogWrite(SPEED_A, mspeed);
  analogWrite(SPEED_B, mspeed);
  delay(600);
  stopEngine();
}

void turnBack(int mspeed) {
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, LOW);
  digitalWrite(DIR_B, HIGH);
  analogWrite(SPEED_A, mspeed);
  analogWrite(SPEED_B, mspeed);
  delay(1000);
  stopEngine();
}

void turnAround() {
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, LOW);
  digitalWrite(DIR_B, HIGH);
  analogWrite(SPEED_A, mspeed);
  analogWrite(SPEED_B, mspeed);
  delay(1200);
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, HIGH);
  digitalWrite(DIR_B, LOW);
  analogWrite(SPEED_A, mspeed);
  analogWrite(SPEED_B, mspeed);
  delay(1000);
  stopEngine();
}

void stopEngine() {
  digitalWrite(BRAKE_A, HIGH);
  digitalWrite(BRAKE_B, HIGH);
  analogWrite(SPEED_A, 0);  
  analogWrite(SPEED_B, 0);
}

void goodCase() {
  rightArm.write(armRUp);
  leftArm.write(armLUp);
  turnAround();
  delay(500);
  rightArm.write(armRDown);
  leftArm.write(armLDown); 
  stopEngine();  
}

void badCase() {
  goBackward(mspeed - 40);
}

void cuteCase() {
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, HIGH);
  digitalWrite(DIR_B, LOW);
  analogWrite(SPEED_A, mspeed);
  analogWrite(SPEED_B, mspeed);
  rightArm.write(armRDown);
  leftArm.write(armLFlex);
  delay(700);
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, LOW);
  digitalWrite(DIR_B, HIGH);
  analogWrite(SPEED_A, mspeed);
  analogWrite(SPEED_B, mspeed);
  rightArm.write(armRFlex);
  leftArm.write(armLDown);
  delay(700);
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, HIGH);
  digitalWrite(DIR_B, LOW);
  analogWrite(SPEED_A, mspeed);
  analogWrite(SPEED_B, mspeed);
  rightArm.write(armRDown);
  leftArm.write(armLFlex);
  delay(700);
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, LOW);
  digitalWrite(DIR_B, HIGH);
  analogWrite(SPEED_A, mspeed);
  analogWrite(SPEED_B, mspeed);
  rightArm.write(armRFlex);
  leftArm.write(armLDown);
  delay(700);
  stopEngine();
  rightArm.write(armRDown);
  leftArm.write(armLDown); 
}

void helloCase() {
  leftArm.write(armLDown);
  rightArm.write(armRUp);
  delay(200);
  rightArm.write(armRFlex);
  delay(200);
  rightArm.write(armRUp);
  delay(200);
  rightArm.write(armRFlex);
  delay(200);
  rightArm.write(armRUp);
  delay(200);
  rightArm.write(armRDown);   
}

void moonWalkCase() {
  turnLeft(mspeed);
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, LOW);
  digitalWrite(DIR_B, LOW);
  analogWrite(SPEED_A, mspeed - 60);
  analogWrite(SPEED_B, mspeed - 60);
  rightArm.write(armRDown);
  leftArm.write(armLFlex);
  delay(550);
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, LOW);
  digitalWrite(DIR_B, LOW);
  analogWrite(SPEED_A, mspeed - 60);
  analogWrite(SPEED_B, mspeed - 60);
  rightArm.write(armRFlex);
  leftArm.write(armLDown);
  delay(550);
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, LOW);
  digitalWrite(DIR_B, LOW);
  analogWrite(SPEED_A, mspeed - 60);
  analogWrite(SPEED_B, mspeed - 60);
  rightArm.write(armRDown);
  leftArm.write(armLFlex);
  delay(550);
  digitalWrite(BRAKE_A, LOW);
  digitalWrite(BRAKE_B, LOW);
  digitalWrite(DIR_A, LOW);
  digitalWrite(DIR_B, LOW
  );
  analogWrite(SPEED_A, mspeed - 60);
  analogWrite(SPEED_B, mspeed - 60);
  rightArm.write(armRFlex);
  leftArm.write(armLDown);
  delay(550);
  stopEngine();
  rightArm.write(armRDown);
  leftArm.write(armLDown);
  goForward(mspeed);
  turnRight(mspeed);
}
