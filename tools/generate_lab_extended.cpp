// SPDX-License-Identifier: GPL-3.0-or-later
// Host-only catalog generator. Run via generate_lab_extended.py.
#include <cstdlib>
#include <iostream>
#include <string>
#include <utility>
#include <vector>
#include "IRac.h"

std::vector<std::pair<int, unsigned>> edges;
void recordEdge(int us, unsigned hz) {
  if (!us) return;
  if (!edges.empty() && (edges.back().first > 0) == (us > 0)) {
    if (us > 0 && edges.back().second != hz) std::exit(4);
    edges.back().first += us;
  } else edges.emplace_back(us, hz);
}

struct Variant { const char *id, *name; decode_type_t protocol; int model; bool toggle; };
const Variant variants[] = {
  {"B01","Amcor",AMCOR,-1,false},
  {"B02","Carrier 64",CARRIER_AC64,-1,false},
  {"B03","Kelvinator",KELVINATOR,-1,false},
  {"B04","Haier 72",HAIER_AC,-1,false},
  {"B05","Haier 160",HAIER_AC160,-1,false},
  {"B06","Haier 176 A",HAIER_AC176,1,false},
  {"B07","Haier 176 B",HAIER_AC176,2,false},
  {"B08","Teknopoint",TEKNOPOINT,2,false},
  {"B09","Bosch 144",BOSCH144,-1,false},
  {"B10","Electra",ELECTRA_AC,-1,false},
  {"B11","Teco",TECO,-1,false},
  {"B12","Voltas",VOLTAS,1,false},
  {"B13","Vestel",VESTEL_AC,-1,false},
  {"B14","Mirage KKG29AC1",MIRAGE,2,false},
  {"B15","Gree YX1FSF",GREE,3,false},
  {"B16","Airwell",AIRWELL,-1,true},
  {"B17","Whirlpool DG11J13A",WHIRLPOOL_AC,1,true},
  {"B18","Whirlpool DG11J191",WHIRLPOOL_AC,2,true},
  {"B19","Trotec 3550",TROTEC_3550,-1,false},
  {"B20","DeLonghi",DELONGHI_AC,-1,false},
  {"B21","Argo WREM2",ARGO,1,false},
  {"B22","Argo WREM3",ARGO,2,false},
  {"B23","Eurom",EUROM,-1,false},
  {"B24","Ecoclim",ECOCLIM,-1,false},
  {"B25","Technibel",TECHNIBEL_AC,-1,false},
  {"B26","Rhoss",RHOSS,-1,false},
  {"B27","Truma",TRUMA,-1,false},
  {"C01","LG GE6711",LG,1,false},
  {"C02","LG2 AKB75215403",LG2,2,false},
  {"C03","LG2 AKB74955603",LG2,3,false},
  {"C04","LG2 AKB73757604",LG2,4,false},
  {"C05","LG 6711A20083V",LG,5,false},
  {"C06","Samsung",SAMSUNG_AC,-1,false},
  {"C07","Daikin 280",DAIKIN,-1,false},
  {"C08","Daikin 128",DAIKIN128,-1,true},
  {"C09","Daikin 152",DAIKIN152,-1,false},
  {"C10","Daikin 160",DAIKIN160,-1,false},
  {"C11","Daikin 176",DAIKIN176,-1,false},
  {"C12","Daikin 312",DAIKIN312,-1,false},
  {"C13","Daikin 216",DAIKIN216,-1,false},
  {"C14","Daikin 64",DAIKIN64,-1,true},
  {"C15","Daikin2",DAIKIN2,-1,false},
  {"C16","Toshiba A",TOSHIBA_AC,0,false},
  {"C17","Toshiba B",TOSHIBA_AC,1,false},
  {"C18","Panasonic LKE",PANASONIC_AC,1,false},
  {"C19","Panasonic NKE",PANASONIC_AC,2,false},
  {"C20","Panasonic DKE",PANASONIC_AC,3,false},
  {"C21","Panasonic JKE",PANASONIC_AC,4,false},
  {"C22","Panasonic CKP",PANASONIC_AC,5,true},
  {"C23","Panasonic RKR",PANASONIC_AC,6,false},
  {"C24","Panasonic 32",PANASONIC_AC32,-1,true},
  {"D01","Fujitsu ARRAH2E",FUJITSU_AC,1,false},
  {"D02","Fujitsu ARDB1",FUJITSU_AC,2,false},
  {"D03","Fujitsu ARREB1E",FUJITSU_AC,3,false},
  {"D04","Fujitsu ARJW2",FUJITSU_AC,4,false},
  {"D05","Fujitsu ARRY4",FUJITSU_AC,5,false},
  {"D06","Fujitsu ARREW4E",FUJITSU_AC,6,false},
  {"D07","Mitsubishi Electric",MITSUBISHI_AC,-1,false},
  {"D08","Mitsubishi 112",MITSUBISHI112,-1,false},
  {"D09","Mitsubishi 136",MITSUBISHI136,-1,false},
  {"D10","Mitsubishi Heavy 88",MITSUBISHI_HEAVY_88,-1,false},
  {"D11","Mitsubishi Heavy 152",MITSUBISHI_HEAVY_152,-1,false},
  {"D12","Hitachi 224",HITACHI_AC,-1,false},
  {"D13","Hitachi AC1 A",HITACHI_AC1,1,true},
  {"D14","Hitachi AC1 B",HITACHI_AC1,2,true},
  {"D15","Hitachi 264",HITACHI_AC264,-1,false},
  {"D16","Hitachi 296",HITACHI_AC296,-1,false},
  {"D17","Hitachi 344",HITACHI_AC344,-1,false},
  {"D18","Hitachi 424",HITACHI_AC424,-1,false},
  {"D19","Sharp A907",SHARP_AC,1,false},
  {"D20","Sharp A705",SHARP_AC,2,false},
  {"D21","Sharp A903",SHARP_AC,3,false},
  {"D22","Sanyo 72",SANYO_AC,-1,false},
  {"D23","Sanyo 88",SANYO_AC88,-1,false},
  {"D24","Corona",CORONA_AC,-1,false},
};

int main() {
  for (const auto &v : variants) {
    for (bool on : {true, false}) {
      if (!on && v.toggle) continue;
      IRac ac(0);
      stdAc::state_t target;
      IRac::initState(&target);
      target.protocol = v.protocol;
      target.model = v.model;
      target.power = on;
      target.mode = stdAc::opmode_t::kCool;
      target.degrees = 24;
      target.celsius = true;
      target.light = true;
      target.beep = true;
      auto previous = target;
      previous.power = !on;
      edges.clear();
      if (!ac.sendAc(target, &previous) || edges.empty() || edges[0].first < 0) {
        std::cerr << "No signal for " << v.id << '\n';
        return 1;
      }
      if (edges.back().first > 0) edges.emplace_back(-1000, 0);
      unsigned hz = edges[0].second;
      unsigned total = 0;
      for (auto e : edges) {
        if (e.first > 0 && e.second != hz) return 2;
        total += std::abs(e.first);
      }
      if (hz < 10000 || hz > 100000 || total >= 2000000 || edges.size() > 4096) return 3;
      const char *action = v.toggle ? "TOGGLE" : on ? "ON" : "OFF";
      std::cout << "{\"id\":\"" << v.id << "-" << action << "\",\"family\":\""
                << v.name << "\",\"action\":\"" << action << "\",\"hz\":" << hz << ",\"us\":[";
      for (size_t i = 0; i < edges.size(); ++i) {
        if (i) std::cout << ',';
        std::cout << std::abs(edges[i].first);
      }
      std::cout << "]}\n";
    }
  }
}
