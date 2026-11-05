// SPDX-License-Identifier: GPL-3.0-or-later
// Compile with IRremoteESP8266's host-side UNIT_TEST implementation.
#include <cstdlib>
#include <iostream>
#include "ir_Airton.h"
#include "ir_Coolix.h"
#include "ir_Goodweather.h"
#include "ir_Gree.h"
#include "ir_Haier.h"
#include "ir_Kelon.h"
#include "ir_Midea.h"
#include "ir_Mirage.h"
#include "ir_Neoclima.h"
#include "ir_Tcl.h"
#include "ir_Trotec.h"

bool first = true;
void dump(const char *id, const char *family, const char *action,
          IRsendTest &send, decode_type_t expected) {
  send.makeDecodeResult();
  IRrecv receiver(0);
  if (!(expected == KELON168 ? receiver.decodeKelon168(&send.capture) : receiver.decode(&send.capture)) || send.capture.decode_type != expected) {
    std::cerr << id << " " << action << ": unexpected decode: "
              << send.capture.decode_type << " expected " << expected << "\n";
    std::exit(1);
  }
  std::cout << (first ? "\n" : ",\n") << "  {\"id\":\"" << id << "-" << action
            << "\",\"family\":\"" << family << "\",\"action\":\"" << action
            << "\",\"hz\":" << send.freq[0] << ",\"us\":[";
  first = false;
  unsigned total = 0;
  for (unsigned i = 0; i <= send.last; ++i) {
    if (!send.output[i] || send.freq[i] != send.freq[0]) std::exit(2);
    total += send.output[i];
    if (i) std::cout << ',';
    std::cout << send.output[i];
  }
  if (total >= 2000000 || send.last >= 4096) std::exit(3);
  std::cout << "]}";
  send.reset();
}

template <typename Ac>
void pair(const char *id, const char *family, Ac &ac, decode_type_t type) {
  for (const bool on : {true, false}) {
    ac.setPower(on);
    ac.send();
    dump(id, family, on ? "ON" : "OFF", ac._irsend, type);
  }
}

int main() {
  std::cout << '[';
  IRCoolixAC coolix(0); coolix.setMode(kCoolixCool); coolix.setTemp(24);
  pair("A01", "Coolix", coolix, COOLIX);
  IRMideaAC midea(0); midea.setMode(kMideaACCool); midea.setTemp(24, true);
  pair("A02", "Midea", midea, MIDEA);
  IRGreeAC gree(0); gree.setMode(kGreeCool); gree.setTemp(24);
  pair("A03", "Gree YAW1F", gree, GREE);
  gree.setModel(YBOFB);
  pair("A04", "Gree YBOFB", gree, GREE);
  IRTcl112Ac tcl(0); tcl.setMode(kTcl112AcCool); tcl.setTemp(24);
  pair("A05", "TCL112", tcl, TCL112AC);
  IRGoodweatherAc good(0); good.setMode(kGoodweatherCool); good.setTemp(24);
  pair("A06", "Goodweather", good, GOODWEATHER);
  IRKelonAc kelon(0); kelon.setMode(kKelonModeCool); kelon.setTemp(24);
  kelon.setTogglePower(true); IRsendTest kelonSender(0); kelonSender.sendKelon(kelon.getRaw());
  dump("A07", "Kelon", "TOGGLE", kelonSender, KELON);
  IRKelon168Ac kelon168(0); kelon168.setMode(kKelon168Cool); kelon168.setTemp(24);
  pair("A08", "Kelon168", kelon168, KELON168);
  IRHaierACYRW02 haier(0); haier.setMode(kHaierAcYrw02Cool); haier.setTemp(24);
  pair("A09", "Haier YRW02", haier, HAIER_AC_YRW02);
  IRNeoclimaAc neo(0); neo.setMode(kNeoclimaCool); neo.setTemp(24);
  pair("A10", "Neoclima", neo, NEOCLIMA);
  IRMirageAc mirage(0); mirage.setMode(kMirageAcCool); mirage.setTemp(24);
  pair("A11", "Mirage", mirage, MIRAGE);
  IRTrotecESP trotec(0); trotec.setMode(kTrotecCool); trotec.setTemp(24);
  pair("A12", "Trotec", trotec, TROTEC);
  IRAirtonAc airton(0); airton.setMode(kAirtonCool); airton.setTemp(24);
  pair("A13", "Airton", airton, AIRTON);
  std::cout << "\n]\n";
}
