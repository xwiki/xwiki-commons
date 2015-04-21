/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.crypto.internal;

import java.security.SecureRandom;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.bouncycastle.crypto.prng.FixedSecureRandom;
import org.bouncycastle.util.encoders.Base64;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;

@Component(staticRegistration = false)
@Singleton
public class FixedSecureRandomProvider implements Provider<SecureRandom>, Initializable
{
    /**
     * The random data below has been generated using {@link FindEntropyForSecureRandomProvider}.
     * The purpose of this static random data is to avoid relying on the entropy of the underlying test machine,
     * since this could be very long if the entropy is too low, and this increase over time if multiple tests are
     * run in a raw.
     */
    private static final byte[] RANDOM_DATA = Base64.decode(
        "/GfIB2UQRMoql0jlk+RVbBnwWNdvm3hwBE1VaxbpvJv9MwdYMVMKNVPcAR72cQHH/wh1IFc0d1weYuHOb+C0Ab4DlRJuSFcyAHzs"
            + "24ymQW88fOkCkHV177edSxjqmNQ0vCnqiPt+kg76g2P0Wlevs/LXunMZ92b2OK6gC2EN0Q+sEs65Ytl/EBK30vcCeWiBc8dp37GQ"
            + "6cyGroZ4w50uSbo2e47YGf08x2BtLNWUcFYoSDfT6HMXm0/mU3izfepZD65pZMXY7Mx/NVCs7yI4Osrw4t41taUo01fwkkXgWvfG"
            + "AFTsoOAAKv3/U7+SQKnDMTlkUHPxe/dikSa1U21zcDbl5MosCqKPSuoUsxFC/hVyn3FIyi9deKE+E7pYBXMjVje9qHfQwxYoZoeA"
            + "ZGZc1rQ31MSzorxZEmK7OHrFekEzy7Ja9hmoIU5a1pz1vQfgnugxydSG6mdMS6W9wENM20TqRihLFzMptoDQAt5IVoMMBs3/svti"
            + "kmUDuFi7HYBGgFBlDKIcHTclO3oEPTT/dXIgLR565JDM/CkTwnMg8z8GWU1Ek4DrVyy1iTO5+8DQgPNuPmJbSGbBBG7gd6w5BYSf"
            + "rvWPL2HzVGPaVSPSH67fllkdy1iMAq94rKQTJLEYUwifyN0FCMCdmiAPmcLn/vWqn4ERwtXCfcPatmDg2hx43bPrpVhVO+O9SYyd"
            + "AmfBsbipgAKIKp84uir1mcuUMOFN7vT9F+vqyraq+SWTBHwhaNkwpcM1JCiVtI/sW/APUzyYU+5XjxfFoBFhoIkJ0nJDSUp0dH8h"
            + "gS1pXxQNybJ7X8naI2DAj4HmWiSXb3ZuIm+Bl3OnXNZkPMjSbuO7AvLnfKlJbtC4VpJ9KRhvY24ACQBe8u8fEaq9duQB4xH4rfaF"
            + "VUufn/vDPS4t0YURtCZIebo+bSyHx0I/e7+XA81ZRPdMbdgezwo7Td1EnMbbNrcTGmsZTBA1Vok7Zp8I9j/t6eAq9n1sCHcErxVJ"
            + "Os7YgWgitnHq6Q1odi9JBwTICsD0x/PZG5qWMpfCZCwyBCm2On2g+dIgDhamtNG56o/fLbq6lm20VrN+vOczIHkj9TXlJw76gAO3"
            + "5yN8e68aYmGmfNcMahj9Yke5Dgtfnm1V2pUtUzw9Ye1p42Y9mIoo/FYLPLQUpSVS5nmWi5cUgGjCIPmhClVw5FDNmi/ng65OX6WT"
            + "1j4mZCFK8+M4D2jP/9CM59y9yylkH3Qh7iYHHTYWPjDmwTVLCB6VBmrMERiXPz35fHhAoRM24m1W2RwX9sbdlMtLw3uC47b9Yll9"
            + "3YWSsb7rNnQFV3zNxWZVMS0mw2cC5uLjrH2nIqkiOWO3jBugG8pcQS9UHybwBFCtEb1I2UqcudGmMA1iSEeiG4+qDeidQbvcaAPl"
            + "IGsKkKgPCqrLSfBQn+I0TH2fEFV0XDpyOjZR9mAUVx9MrBfOhOmlYB9RuU+p8cvezRwsVicyntTll4ETaCSecqmLpF9XuhwY3uUG"
            + "rrnUPeozOlvRVJUGPiSkLLGwXSLO22zEEvtaL9EJXNDBoRwS12nInIu1wi+sAK/bd6klpE5iMqJhr78rDPS+XtYA3SSB3/Ej7abA"
            + "mbNBSPX+rwPhlysivAfd+2guYJFI8kR2CeberjQK7Hea2oIr4tvspZszF23sNw2Md+3lb0kPrRvAhHCdVGZ0k5ICjPoixPpNh/v/"
            + "NhyiuoWwQCp5Njwhf396P+vMDFm07mX/Zy4BVjwHqBV2gkEFVoJG3Xnirqr+YHA7UPGrsd/osvrCelnkFK7JQ7EkSGjFYMIxNjYB"
            + "60krvyoVJTNho8g88m9Ub7g4Rywy79WPxJv+/ApadMruQJ76CDflSq9x9APai+lM+XDnE7y98iXI3zTp8DU/H986nv+CFNIcGlTv"
            + "kmYy+4Trg8JY+lDxYqM0M4dZGffqzPOAEu9NWDY7ZB3tGOfYTG92qOlqjQGMhh3IrbaPlaleyE0jJN9GCpaOKTskV88g4KwChubj"
            + "/YjN2NUC8W1xcUJudTloNAwRWEuxfmE7fDE+H/aeCaWpb8664ASiWohl/IyEre7nrJTToG9XpZRbaUdUfjqANcj0XM+ck8CItazS"
            + "0Z17+6LVxduqNtJngTdtoIm/SiGkJ5iCwJkG4cyhc29OpyjDTB4bXMOYiqZHMAppjU6P3QFQYHM4CNObwqoEjMMWM8/ACoOPMdMk"
            + "/sX3XSIRJfIKdcxTFzO2Csd3cFgx+hOjDGP9nRSdxsYKJM7Fo0AVdevKLLAu1IMP0r3fj09sbLz8NPBfhjKdR3onHhRBAatHeTc2"
            + "mh9pm4uHcuqLeDKBBpTn52WzMsiepTjm6Mc1jXNXkUDRkYVKfbO5SgIL1RWyAGhQz1hR0nDkqoaNnU6PtboLwmjFPId5NM4Sn8d0"
            + "XlfSF4OXVijTADcdRBwJwudrliVIcTvtJTRqHaTkgI0Ur4a+hoj1P84Plljr974NkPx/V2G7Wmcj346pHbwcp4HE5S5BnYKbx4Zy"
            + "CuHmxZwjfqAJa+ity70+oP+5HCo6SPktR/xOdcdwiwzFa1xcT//e19DnxWq+5OheGyN5YSAmGaKwev6JLv01/4CaMD8Vt64aKKpA"
            + "LVOEpMLRNORQ/pQcgjRYCNRcqvd28/QDH+Q1vUpcBVHKwD8YPmUbov0dK0c+lX2BtN2XsL+nQYRIwWurXi11PUwIPBiEBvlimG7e"
            + "Zl0HPhao4ZDeejVPkwiA82EU3F/j1IdSE74M22l2RPfnZ5Uvf4YEAwAdR8BA/vzqh8DtNUKyMgDLqmEmlG++z9pFTGysGbDXxAGO"
            + "FbDZ8FxDAXyOqLWV439uXPp0smePwB9UrEe2HurHVHKmj9l42LD74XOwYgp2HVJh+UDDK2ZTIhc+XbZuhaqW2HG0twIEZAkGsvVd"
            + "2XkWI+QcM6UQ1VWVU91sQBMXBcZ7U1UKXXFKcht8mQ9kQ5e9qQL1NNOvFb3rE6LvRWiau+N77bRrQRNbgfOhBQL4nSMU0ibaYkbV"
            + "KwJFIPAJxtmAxFsBKbz1PkE3eDs9yeIeXq9CYT5DaFGVDD0YpBZWFHnqXSJpu3VKzWmCU6SdiXjAbedt96qPQ6/u0mDXrF+jTfsu"
            + "R/FTZdVavuJol8H0Mgr+x4p2ORAeVo1rL3vMtKr/xUko7HQp21XI7nqjF7PznNHxMUdNLG5YA2wiGOi6Rflz3Luy6aLVdc/abdRL"
            + "6RtZa4zTfkMql16C8GchzElP9sfXdTtI1bTNLpum+YL4jAhS+/LThHRGcWIRmsD8PWe5JPvAOXohQCUyuQudTfm9InNb0k/HWIU0"
            + "acf0EfT0UHjmwh0n/EApvgpg0rKim38ewsLj0bzFatc39puH73vl07N4qLsoNIjnzdzmHWxk2WOdoxBD4bDQPI4z1rzp2XPg0/AI"
            + "486dNsUAMj7oFzBGTpR32Lof0cPceE9ORfFIxfVESoRLFCFt/scJjHaas+bX/K6P5dJPyi+oycRgTd5iweGek63J/Xj0M1kcawOU"
            + "oU/6ASxFwbFnO75Jyd4Dk6YpQY8ZZxvgOr4RJwd97surRUnUFYUkyqcj5a+2yQhNSOZv4JJJoqv6a12645wz5pnIlZonQeApmj9x"
            + "aHOybPwlqqBt1j5OWxWJQcwm3Y98gBgsHZynIylr/ZtBrWoSZAEnTTjo70uAG9SK+GW1Y+r6wzxk7VDwe6C1Kp2CfqL++j99+8Tj"
            + "MU2lxyWNR5hGBr5wkBIUvNm0x28JpR772sVHSfUa0Bn7Tj6TSnXEjzLs2XeCH7O91Wx1XJ3V4b4doZkbvTT1rTtSEDPahDxHtXlB"
            + "AcH+HZVBO9pUbvP72x8mP3l+YmhJPOGPPwE5YpQa4rLVbnlC4oxNhd0M5AEQCxrGkEUtQ3Qrv7dQ6mIjEELXSOw0aQWwnU/8K7qR"
            + "RXGbZU0wSYPLeWEuF5MrTNZcBPUw9ju+GOs+fbTAwli+IFF3VnX0H6kD64VknseYmhwgKfMPdf3WL+0ZVhWdFpt7YyMEbg13kQNe"
            + "xGzzy1lXq9MFj+K3C4ljdajHoNPSauJI+mOTVjkqIt+jvXy6S850hE/6CmPJBgFC+i9BIYk0K2fk03nm1/Zx5UZU/qOLj4UJeGXu"
            + "aGUEr0SrSHJUMgKqbx91M5F3QfY8n6PFNtfh4+xLhY2Kid8ZAceCp8XiWQ5V0YXEB3uYZGumoZ1tBhNoq8q5+fSVKbOxfl9iSkXC"
            + "Adpjs2So7i4oeHraulS/GE9QtJr+VCPoU8lxvVVAIvgozqIUHPwKErs4cQUl3WgzOwtkMLYh+KhLbWilqMaKMuj0jywGnN6QM3Cf"
            + "cZPOT4yF/En873thSYVq5yBw+UpZL5vRbF8kaciFcQuO0wsFkhTZycukhPnppc4l3qHFAf32qioGHqu2VPjrVId5mtJYtd9AATnX"
            + "dw/NY1ZNCsN0UoXprpOoxdb70c1sDPmeQCWcgiCIDnySRwsVRAteg55ZcUlsLX32htYhxKcQ1PI5dILu5vvoeiz9qqjsRtYGxhFA"
            + "ir9LJaGSzFiM90avne5NT4DzIM/9H+Z3KIvG6/bsP1xtWwEgeFIg4OTbh47tMgh+rjO2Hgi1Uv0n+NICbRzzpK0CFBU64WhiOVes"
            + "r9bsflLS46CSbHNw0wyJV+56uaKk8cXGSQ5lIjuDpG2/0C8eoHtwiqD1Nmw3IwfX+yoDHrZ3V5ths9u1FLlgHw5/ledVSgv1b4Kd"
            + "WpnLIZLqnUhhTzkhMFirQSOcjL3qQapieDyRqtZwYtuUDDk6eJsOLvzYP2nVlg2oESiqIkZgJGaOigFYVg2jipvyIF7FvO/3x8XX"
            + "CVmydPOR+kkFxNESTrH5ZmQ1EuqOO9AziULfqr1Mi67ez3v6Gaob7EcmMXg+vuuT3tQFJCtevoTg5f+zxUaUgUWqzJq8lbDdlSSd"
            + "FnPG1oPP1XfPY2SY+fOdbIPBTDyMT52Z/59As23wK/Me8drJOd7dinaV5wXDK3iqPCyTHBCUj/e83+hqxYObBdMPMMePeeWneqd2"
            + "LlSvqRE5M/SuSat6Mpl2UV1zUi8QMzD5BOTdfvRzC6bpqzVsOKkJlfIiwYj5ZtxA7B91e9qIDKbUee9NpnJfa1GreKi2S3EGFn96"
            + "HgS48ts4dOrUGky8bu9C6U9tYlkwqDHKYfoz501C1gAtbFrezmap3vh5GT401Y1S6IOf7aZLq5QOoYNiiiwk3GSt+dXuavu5QcvA"
            + "XcvDLCbtj3dTa4/+6qFmXeU3s8z+sBF7EVcTH4Mep5KnAsktsYt4PTG37dpy+jM/l626DIgy693TAPTVNPh3UIuvna7mVmohziSN"
            + "WTUwiKRQpNfpZ0fP8MgYmuikXoPvRzOKW78pfvlAx7dgbzbDC3wng8iNhG1XGfbVMCmP27XQu6QGvOnd6sXA3gej1qBVtDCkHBUB"
            + "KsXsFUMPubUNWCq0ZNe1XXnuA+rqANUbkeWyi8nxTvyaNwPTUVc6kPjyOUCxa6+KtJrpAcaqThoTGO8XlqyroPeCad91c/bMi7yZ"
            + "OLfg8WG0lD7BBpxiR6d6mqjNXBD57LtW1huBBq8vaqbmb6e282htVGnnBT+aczy5dt9B0+qNqGHBzx6k5xJcEbLnST/jc0/zx7/1"
            + "tKaO5k0w5TTtRwS9uSZNoujie/26Vag87dMtd2FRIRWw4f7C8F96UuvyUBfilp+S3LeSwPJTL+r0mypQ5EEKMJ46OOC7GCpOy/F1"
            + "zRxPVXtDlr7ooNcobq0s/LFiBayf712OU+oM/PAXXbcYq8MOOgY6L2D7iFuDCKv0LtgvwU44ph9A+ujaVFp7Kr8a3InAId+hfUw8"
            + "qbYJBcme2dGdbkxdOmOxP5WkkWaV4NUHCUtF8nbNWd3hcvPr2bDWV7lsc6Dquo48TK7ixhqMVfagzvx+OM9fjJeUKrDdt15EVbNs"
            + "oWuH0lF6KPJJBT1tcLZW/8P674FhBHDdQ86cj8ztdn7ag9HKqDE73nW2GuGMxKDZXZCtdGBG50cPo5kZACaUfcvP2vsumwYkRPVT"
            + "fqi/QNQG0mygX9ZWxEROdLiwxlE9ek2HiXsxSxXQn57eAxT/0pwTp0CrBGtnbpLshyXqBYYBXH6bnfI1cLX+gDvZ2IF9NGynSk2m"
            + "uKt7eLQPxWKfV5ni18FSBd4Q46vGJbLiDIV2n2330NdVtYhJK8fSX0sM6+GFs4Wi8SfmZPnXrj0sD8BzgUUNKcTrxMWK3sXHRp/1"
            + "fDc7sg9h+8gKflB79sXpZ4yQ9lHvPOT97nEnfcsujdSXNr5i2aO6wk2+KMp3T6IqTmWdsveiBZjTyY6E5daWMHnBYKf5LyEp+dk1"
            + "7qkkBgFdMs9dOJ5moEa1Ch8oGU4al2s4UG3ybygqbt/xgN6tkiqDXJWZPUkzOC4uIFoJkQDH8z1o8s76lbdMjL1lmSqhFp8ZZWBe"
            + "H8byi1qaBK60luFuzC41yBZYvAs+uln8Mmp83nLa4cItPdkZMvbEo9MZ5+hewn1gPTNa3yGXa7D3+TWpbaLfjtjj5UPWWzelDJ7G"
            + "KdE/P2L/sDoas7SbHIS91/+jlTRwsQgvBtU//WAo28rFfGVrH+ee2glHWr/cTDbw+sqNkLzGhb1ItU3fB5k90OGnTQj4kh0JK2mb"
            + "C/Teuh0CHGLpcri4XFeAZDd6zX/SxJPEpDxU8Z16LNn1M8ijseYwskx5Mue+86lI1PdznkSgCaxtn99nnmj7tjJUxafCO4owYI/q"
            + "jS1fQQEGcqcQyGAm3Dm9/YcbLoYHcBPFJrByGGVXRedyONPvXWzqv0CP8kRnX2wY67W1PS8v5LIWUFydFG3rt1leybcEX9dKpHUg"
            + "QgjHAVo/LVHvX9Yar5wRG/csWEwguKg23GOkTzcwoV4I8lQJKzjRphfJ//psNDCIgYetQpe2E5Q9K1PFr63CyVhSMGKPyTTjV3Ay"
            + "7BVZvi25qtVLs/JZe/XskLt03657H7scBz+VTLSFvHMrMuQy0Jr9sgyG8LKVnSwB7xVfBF4adn5jdn7NpLHRmHtT7rv9jsZiE4yS"
            + "BuFN7VhLRldkxSowB3NZGgeBrMT6auAkdAMWbj+lnVWIegxbDt65YJUTDQw93Jn9tkldgwSUL6jH+5VCyXXt0JJya9UFrym7zW1N"
            + "q3ZSci9n96HcS0E30tUyqEcwjf8kGq6Cx7op3qLeNiLNGQac+dtMcJNSV+NQnb9vDHlJLXctZ8p+6743mp2AJUgsFFJBGdSJatkR"
            + "uap98PepRb6z1+a2trtJ65tuDZA9mRY/+plHjv4EYOCJQmT0kJbomxF2GIBd/TkmDZRkC7uj40AIcxw/1wu5dCog2YGSujgDFw2L"
            + "nAtxQkUoMWXLF98Mudjcg2XATKtRnKu7vpgY6W4vHZP2uSNYdYzEilDVPetzRRLFedfZYzDMrK++JRGdbMHHjrr0ry2lSod7Q5kI"
            + "RbdPampC4i6Ei2TtmgaBAzAvTNMZXfkQ5M0JLoSsWErxTJVa1kNjLSHmzkFqqVmNDs8ui1NTGYJWsaiWm07CVxFUGOy3H/4Wycio"
            + "X+puAWBoEcqKmlI92R7uupoE6WSNc5k7dpbrI4xhLuhEIk1w6Z+OepuFnYjD8dU32JaEPqwzcriYsNx6MpOBFiqtkzK73uBd4Xhs"
            + "p2fi7NLFkAqWse327WM/aPOj+f78/Dcv9KqNfhg2grXV3/AbfOv8AimXoGTtrApz1Gla3A6icL5wNfTdXHRh1GEtdDtW4LQUci8F"
            + "frGaQsDOsg5uVf8wGO3SFn3HDY/FjwCqhvX5+TyFyHAmDmfyTK8XO5UxHftWkH/ImrRX+CJYgu2d/RHYhTo77RlaUibK/r0Gtbux"
            + "mcr9YHxIJGXz5mWM+z49FrfXoaYz/ZM6DTj9XWg5BU0WUSJYiux3FdMEsPIzFwMKXXeZXTMgm1ouOZ8OoJYio+/3EAcLqHqzi30w"
            + "xeO1IMtqurKAXjZszu0g5dbxOP40VgtaoxRi9yN4wkZXWs0kW3Xqnd5KDerH5B1Qsdcx0dG0JF5mhWU2Im88oByzSf5AEQw31qbA"
            + "GhB4WEhuilBanwi/2X5kGqLiktPs48VK0BwKy2La/bs9FC0S1NbcN2zjch5MJXwgX7CXEjGbQh0dCddQ5oZaODLRLqhVJwuIPIwS"
            + "B0jvb4iYDdIGIktWK+1HwtCr65Bec42gFlI2uM5NHRYcd0p3FSZrU8rszOn+YlyUgZxaDG3rbHmsJDVzJPf4spjKVmG/rTmMfQ6L"
            + "EdXKeu0KCwOHO00KHttror4wndb4jZOkjZRqzhpd0LSsA92ESc5DfUAS7dgQsEc+DwGhHaV0shgRPlE125sB75iy7XBzbS/n2ZZm"
            + "EB6C0w+YmdWV6Q8jBclJsg1n5Pu2O6aqGa2bb01Pm6o/HL5rZ7cXYTrRlcOp2u5/YGHxJj8PDY2AKbEu6OOAxPjVANnE5sYwpL8S"
            + "HZ82r6Pc39YjVKpNwP14oR07awb+LlHP2Rm39A+ZxzDWnDF12G7EC214kc450cxATDtKXHbh3FYY/Rd6sEAYbWdi9VcrgGYfSJSc"
            + "1Rala6548Dxrr87U2KThEfjmb3Czu9O5jzNFsqyDjXT0M2o4gVA9FUyvZGdqKYVdX2zkFNpMRDDveTR9AeGY04A365HT/xAFcQ/j"
            + "HOg15hbxezw5PcBMRHGWoEXtD+W0e8xLIWRcwR/DnOQK3CcR/KBI0E1jFgqh652SN0JpDGzB4ytt6gWEkq+/U84NuEabGcMfDTMF"
            + "+pfaoLUh3DW0VO2qub6vrRLKsYBwcxQgkazNhW4PLFoq81C6YtXGKSP3Ysv3PEA0NK7YuE8s8FQtivl3VBNShBWJ1Hy4p0Zn+PSV"
            + "t7uGnwLMHwQK+zVRhYncN0851fMAdcJHOXCv9NNbBXCneHLu0RclF0cu2fdPpf9OaXKObMN2TdxuMRCvxJIk7+C8JIm9Eali2jzk"
            + "ITNEkfRWJuL/73dD8zffNWaCrX4g7WFBDOi9c7ZWpKK2Vnj4RXE218hvDtS3EVMUL3V5KWDE6Q+AALM8a72RRo4OtaGYtAowS7Nj"
            + "h5ngV1DjhiHdoVXV4Os1Cv/C3HaB01iZ4hTMw1y+zqnc8e6EYuPBaHdsa+g1Lraimb/FGfit0Ntj3VR2f2DTQvOPtGmLbdYTfOnf"
            + "kifuKT9CORQfe8tMD92hQdXVJD+yukFosB8kkXSSL+s8Q0SAoUpJl6lcqapg7hJOV1FwcIAkbt4Ftj+hB+Kt0Ham84xoRwGArH6X"
            + "21kLcM7KgHrZGkwE9mgmwLpBoFsMRiASIsDcW5HxErwRIDc4drzJPb++qW8KWokltQR7RKmIGgDIEFeYW6m3PUQH59oI+4+wGsSy"
            + "TPC0bRCqWrItOWuTlw0LulE7DeBQJIifgN4v0UBaT/hcMw6TV+gs2QHbtydjMiZiIFUFeF4sm6pGrHMSPIZLbZQyH/24L6uK3q5s"
            + "CqcOFUqk9nnrWWfcJniORL/2EqgyZ+ffIrTv94Nc/y7ADK/OERLuq9bVYdkk7cirnUS1l1wggA5GX0BvAVn36zkV/VvIn2LdVo7u"
            + "Git6BPo0aQXzr9mMijnquNmyZLsiiUHJYJSarpNDXbszL3NNWfPcLhAlzsQAN3DoEb+3SYWnZ1/MV9IivG/g3e81mB82o1p/edaf"
            + "oZaJLgn4syg6fGyYcqXkP41p0HSfg9hU0XNQK2rn4IedKhmHuto37aRj2BFHZa4dFN1hr+ety917Q4TQOy9hBl5ehAL7oBPBYWGL"
            + "Xmcdzis9MKwqgXJ1Lh3use8dLPD3fwulZC0vJd6iyaC2vLjbX6/qGarn7CSHBf+S2uKH7KpuS0YgjEeVsBN2FMnaTj6jhvYL/M+r"
            + "gha75df4+SQ+t5IZXqBzmCjtO7VXrUAxnM2Wohpmo3qNT4Nh+y2kYzW1nxJPbyGsat2LmFSNX6tun50FemM0DvYowHBeL51zaFjR"
            + "X1CsuSQZqBZ/Og9MACwBW36IuhLv9Bhh7WQ8k/nzztw0kTZoEN0ZulbOo1e0+MK9IBEYffjb+tnzmVDE6xaq1MBMQhvBv0Pt0loR"
            + "aVsY2sn8L6DmSk2nY1wAfITwBA5U9eoltkzdHB44KLJlcP2LZEolLhlly6MbwbbN+zIV8CfzJ/FCJAaGm4qQhoALaUmO5oa0ZiUj"
            + "kmLYS+Efu/cMp6ts3bRcTuVYAU/Wsrt9DLPrLWJVB3AORcLq3LOWlETFhbXpvpb834X4EcAqJrKe9pT3VbADzfPOxgxispvhZvGr"
            + "KTIxAJKG7PPO/SK7HegcC5pz/P5UeaII+Syh2hy/bHLy4F/LltDMh19Tx6S7c33/nn1G7LMeaZWhqGpeyw26KLu8uyvT99Mf7xmg"
            + "ck9RphOkp9/nL/gIuudduGwlDRIQ6VQlTrl2c8VWnT9h3m7xXzX00h+eFb8tqUS1wnSVKHvaO0jDJkB5l9Uf6e8sGvVAm/0Yc+Bq"
            + "oyS8QgOIti+XjTXIuppTw299lew0XxtyEUQDqTacuBg9KdKlBTsEpq1QjNWlEISEZHjyKBTnJg4PJ4vyOuIPp8bqCz1V9g5MENct"
            + "QUPgWO24zJWlVHuhb/Yb6l3ygX2Gqj0m9Ttau00eiQTiUB/nPqhuCnOhZiJrLrgAdqWtYr8uQ5k+q26RLnEgnSnRDxy4H8jI2jc0"
            + "qgw5th2HufiQwVlaVJUsnmifV/3P9n779hnaLT6jiJZ4OWa3eJZgtsuSCI79wgoBj3hAn5QhoWpCHREAgjshDbQjErXbM95Morwd"
            + "Pnmj+O7ho73Xkt5XKZosHzvAy/lSDxnKshcOLXiSxXD0nADM1Yig0mXyqTWgZylVD7so20YEKExopagNbMYcpJp4BwfyvlCCggMW"
            + "T+efBHpFx+FReOH0nSFKfVFpXWqSMDm5YkXjY9FzbqmuojkY6TsIW4Kwioag26XrvpbdFYIugbvP7u8JhZL91z7uSIGfLwjv4GMb"
            + "uY8K6T/yn+1BQek5KYzFr5l+LBTBPF7B9OAxFNxNca6KMCwnlT/hdAkzuX36Jbs1OaRnBNh3mWCTjQYN0n0zWNPSA4sTqc3ubNXC"
            + "RRagZa6SIdoF+p4Fdt1OuiES8E6/NpGxo37zNFdYPy51hG8sC0K9aQqsT7HP0lEISrX5DAEve43siMBHZJMANbNGIIRXyhYaYP1w"
            + "3D8r561ZgkWziUZaFmTNWHze0T9/efq5u6EUOoHBz87JEbmxTEwyGV+rHl6hCM07gNnQNHzDc2qWf/7/37IyzuJJ1Iw5vlxtDEGH"
            + "AaGnXrhKaRvaBjjqugjmQtcYnFiYycrbm1Bg0CXWwCAJfspms0yddfHn6IRK3OkrDb/YrQ6TVTwxEKFIQAmG9BkHiXSLHwVWcHiJ"
            + "BL+atZ+Frxdb6WyRoK37UTV3ZjRMIiYARRAAx475fY/3XDZIkdYKPnECWSwwomRvKXowsmMeTiuxG0Jl0KRKu2ZUsefQDt5g5aRW"
            + "pZFnzjdkTgQLswUhMqxxQHOCrErjxajSnAXHtWQWYBdBbssv37OFMXokzLZVfhHtCGciqigahFLU2zr+O7xjsHcSF9cCxUcz939g"
            + "1nvrJJq60Q77IKxHJwGlsAlRatG4lv7ZL74Qbg9u/Kpk/J3zLXW85+r1NZp3FJN7Nkui8Rpm62en4deIfKUDO5Fn458aiOOxv1op"
            + "I4Q8ZzMRxlF73fhHDXkOlbNoFzxmkh2aHTWps+KEX+CzIh0KIea6ZVNZtrjoLY1vPtn7tdem+5bkJ34JH0n2sXS/AGF2VBIu0bZE"
            + "zt2AXRCdoLP2CGjGzZQ7S97677oVeg5MVyYe0L1LMpW0bYzySZnfZFKKwci/dvYN+j+IousZWTm2kprNT0NJAhRVtvvf5W+KNjkr"
            + "L59cvDd5lyfyz35GUIG8J7bffb141tL8Uq+Q2pu/lpDILFaNZm8/I56ZDkmyLT4xVeDwaLSjEdieMgaYV55a9y+UOIkn7S+JdM+z"
            + "CsKUdi0kRo6el6PjlWiXTbxRKBrLwNAYraSasgLJ5k3SiOmzYzeMa4bosiW9pUK2skRzpOQHHFfYPWHv0wZQZK19DBtMZc/ZeH9f"
            + "eLpmmPLrj6QeDnfSEui6Upkh70U8nseUeyKGgIo4dPKhs9jlMHIZ4F8uJSQEsB3sTf2Ex/G1niEbq/AaEb/LF1v1eCs/9IGg95sC"
            + "KQ7d24pD5fQYBn1Aivtt2R96uKIHOq/CvFXtAZcWWPT/d1tP3JcjSdj0iGM92pR+ay4/vadQgBxlINDyDaJp1skSfXpbHaXNbyOl"
            + "miD5lqu4QaHATCYLqUzYg9/i0Wk41p1FFZRzfk9dpDnaPxlOSgKeYQgM4Y3aSGlhJvKjD7IZ1SYmWfc7V3g0gsXVRPpFMP2M9eM+"
            + "t8cVFmDrWojyzbok1SlvoZ7R8xaihD0G4WuAvcqrdKoN8b4JXaMlHQNZwenVB4e/Go4TiYRvkep0PAmG55HOjtE48V5Q964VNE/U"
            + "hxdtyIkHfcKr4ZVEQNc7OPi72AqU+AK8Nt8LiWPHzzZLjWLXhJroYmWF6zou8wbgMSyqJXGg/Hh4wSKQoCdcp+yOT6qK6dF+WkEH"
            + "oNBG1SLnMzqJsx1+9NWutuIYqDA44RTzxulrmXK65SIYfPrZtMbcQTvchXt11U1/A+qUoo+fA/xUarFhMZqbpGVFmm17enTAQ1Yr"
            + "6qr81nv2rbhoePp5p/ZLOOx7UP2tBV0X4GXUS1ItQec6uSZwGuo1q4a2PzUN1+xcmSuXEnRHh7JStWU1KFOJPv45MlG0jdlwnjZI"
            + "+OsixEULS481++y2lLg6gLZSaCLfxZif7tUoOErkHjB4XUa3weNgAqEDJA7NVlAPZ6lohP3KdZfXU2PqupcHVOg8bDOo+xDBW4Jm"
            + "qfx2FE1nOC74EOq9Bsh59Nf5ZoVi8k1a2AQZtZK4M+1reAVdOYTdZpWHbUcviMSbJ6aLvVNhWs7pMvszE0uR3n+Kp8HWLpwD8a2r"
            + "POZ5rWdHpFhirDfq3rZSjUAMvFGV8Gac6M8cPkJTG+YHgYdZTudO1uPB0vjqORCpaJrHTQ3XyjXTXWGR8rQsidI+0G66wpy4RNVh"
            + "apHt5w8ieQ24eaj09hhkMdkmvvgsLwuvw2JodSOKQWj1n8jg20/RyEBotRRDO5/Rvop4kbJAVq+yOQiCVRrn9F+2kR6VKCsbAlm+"
            + "OISI+l04nL5Un/i0DV8SDdVBm8VBe6ObzOnj00GulUShwp/VQmyPjo4cDEMS4Fcn8+d9KA/um8tWwDv07IOyVy0dfaMWE3MFqiih"
            + "a1MZHZ/44DT5dIJ3S80sR+hTxB7AAlRYvOBCcmosfMNCAgui+d7HoU691LcaK5tcre0NzJQOeumeOUqZVasgbSlIe7u6qToRhRuq"
            + "AEi1xeTX/x4t9LpN5Vw7wj+Poy+wNa1ycdT550OLY+6f/ohXCsNqCBk3hWNCG3rg1uxIVEE5jcqXDv0JDEhFSBMFyKXrhwjxR1On"
            + "9yxLRuunQzjkxK3csbPk0WO4QvwCQT8PjxRqpnWZkOnoCt7DtvDKw6PY+q+cDQIg0jLavF5QNrHQ9IZJmIAiWp+A7SVPpj64JVcj"
            + "9xjNgSQOS85SMW4/CVkrxhAOoedozNkCccyh0bx5g1qrJGIPMkgCvSqPCSe7tDFYcl9mzgK/KwQml0VBzjv83H4oLOHcQrG0BXzO"
            + "SPIkUy4Zkm2ry3ccoHXAcfHo0C2ksDwcTIxAWu3CLZsTZEWYK3O0ijZXt3M7tANf7K3Fdgteto30AKRQ5Kp1npHDu6R1L4PEIQxz"
            + "IUJB36UnGwS8JakaqWp6k/sw/0lUYaAQMX9QZ81RkJ8Zhn3dK5nOtzJhoNYPd0R6ZVqQ6twdz+sSJ+IaSGm2U2GkVCxrDRdVpFts"
            + "guDWKfGoAgY/44nbtAFm+PsJIKZ6xaLSHaIqMXIW2ouxkKI8PHx5OqtLtPj44RdKIOCP78BFfYS3xZ/to4oCvzfE2fvVVoW8oYiR"
            + "K4BYS1u8dFAeWvJTkXcrvs6e77ud+9uRZsqWCo5ItF68Dpc0sd3rLUW+ZfkwEkABf5LbXD2neJ+5E9anp/N7YFitW20Ls6JoAxoy"
            + "eHWVStwtQLtfa3lOMsOqqbIopVIt2WNyNaXPvqVWci+BIlvWznC2ePv1gFY7wuMY4LBORv0f7HIk+GnxgkkQXl2u5CGZLeSf6ojx"
            + "ApazhL3i9yJ8Tx3ePqD83pmuXuF4/++YJE3sB/BI7XnWCHjL/JXHEFJYN8t/9QJ8uxl167/toEbg6D36akqaBHYwIMNvDUZ1paT3"
            + "AZh9BOfoAEqoCOnj09oPaFjDy71xfMSc0VUc/1LsJs7BbqNIeITkwkV5rqYg31mqFk8VXXeQWCLDl4ovEDJ0h92PmsBwo3w74F/N"
            + "yb+2Kyq+ND5h04sLRZT3eBI42ezu4cPjJ2g9MghGi7HGw3PAoA7IWsi+4ikYRT9SYTkaTxCxCt2twVrnfCoBiRerUjignKG3BvfH"
            + "/sikKPAoZcUvX6bhdCZl/xlH9M4CRa39lut9KpcxXkcaVIYdMU5ex+TzCko8MIQSijm2RM1JXwPkrGG6RN34ch2SeFcirFDkSlge"
            + "4vtf53dnDFf7CcXtmRkEpeZ8E+W8x180LCo0AJHx+66ZZyrToCA+R0Bj3VRuzFuE3WwtUkngc8gCReJg2dKmXp6t9zhJrfyt649R"
            + "24+v+VX7v2+NUcF6ShhMx+7dvIxBd0PhK8oj7q3pa9imh44EKbBv/d+x/RiCjXlSmEMoDeAPxY+MH9e+Xy54y0ZE5XF8DYZkx+Fr"
            + "gjQeMp/buiPhhgc2YE8k0/OQEXcWCAeqgVk3pEvyjdkc9jDTYCcVG6oNz2U8ysI1t1J5cVifMxXc7jPdf9TZeXCPCyOXbcfoukwd"
            + "cW618Ej7DpthbrGMWNUBxJyTPkzhYB99gg8NHRffZ0GAc06twIntTcs10Aq3eI99XdLhngId+ZWd2wz6qkeDzxN7AxuKjHtuNSA2"
            + "+jM85It0eFe/Qg1gy0CU7dyWCTTi0W9/ts0rlkloaXMhC/sWL1dkQvN5z5cZVz9XjMHSj8H7Sr1G8GV3gRq8qdyiIwlqmsJLti7Y"
            + "U/V6BxXdRhFKfVU7UKTtEEQdQP4aIj+4+eE1cnDCBlnWQUnhM4PLnphMsNFxcjG0/OjwPXc+EEDYKe969xxT7+bZSGlZ558Jm5+0"
            + "bTV5SQ5OgtlBKRMqA9eDZO/6YVzxVeLptVnr7UNpPHZYIwXslNPKp18jNG3LJGjKjQy8d8YaWzwHCEvgvvxRwkslQLwtIJe3o/VK"
            + "yQqedAhl6tceuwZDNrStfMrtO6MmuBmZf0/VnOS6N0iDOl/ABfVuMvvl70RSPOioeJGBrSKoT5CrPNFdNEC3Ocr9BtGxdO+wvhzg"
            + "v9ncMYSU6+YdutZtXuQwUX8MM2kunvm0l7Ul7wjfDMKc4Mp5usUHC4ufLn2lxsUEyNZwC5j0zdDzI8rfpWvvj9hx3NDTM5mdh8X7"
            + "rVtieB9camXF8ddhHm+WypLiU3lKQCnZncPo5qyVLkGtnts8IV12jqO713z+swO+XxDXATAMnIKJ618MhBIS7vOPhf+NbvbpLwZP"
            + "WbPAuZySzumxa9n+4ZGgQEWzxNfdZ1ZEhV1ZTm8lXWLYvOcXfo73IaObubAI3o6xXQVZz7QcPUxaW0fa5RFBZ/3j/LkVj624WfmE"
            + "LVArTk3R56P5KhyPToGplR2gLWNsohosraABkRkIVoAerZ33VSg0JnntaFBeKEfFRtKs1AZ4SMRWccDnuVbuh1pN4BPTOFzJ1UwW"
            + "PC2qxk8ARNByz0huAImMWg3FhkuQ9vAzDhViKVH2YfAqripVRpjVLXtG6JdE4keuKKZzrHeqqIQhY0fPT62FVrQrYzipXgfQiu0W"
            + "T5JePtmayQrshfUmmMAS6J1gqoI4hs7DSZ5jFDJTp8ZqwUD0e86KAe/6hnncj2u16y8zCY0FXNG3LYHEBGLZ2T7yVzg0ikJDNlUE"
            + "PH/+7sEA8AMMkuxGODBQGOnmyQvgR+M3lD05ulxesJTEL2HB5EX1xpFymCLjN1BBZyH+Zg+ZwIO+1jSHQuTLanKhcnfPQ96U+MxS"
            + "En0QusyjUKE7DOKxS+fGX73jYGQALRvhE79/HGQdzOx6MuvKlSiJi5gIuUM6vfXicsk/ozd+TvBlDG+IYAbniCLHqRhKI8J4g5Cz"
            + "Mjvn82qST0yaROa7GyRQGHAZhXxNEvyKQkoA1OvtPpXRceqKuoqH+inh2DchtCvLd+MYHAUO3kLVAml1fgCDS5E7Ry1CGbKIrUzD"
            + "aYU9FCqsX/TjF/du8gpBFLCVMi2xXW7gnWfNKyB2pPX1ekHD0NgqH6oDFNKHPwDdKqnaqC0petAseyTwZfjLN9MqCMIp3qQcWrYF"
            + "wL0bTn+PPyxoz2KKPKiFr6aj0zpH6DFHpNeTI0hkhVp5Lq/fEesFDSJ3SNMzXWLuG2wcCYpYKYrO/JaILRSGJ8ps+6znle0b9ZIq"
            + "d+Si0DDxFwYXEU9rJ3n+hO0vBgZRIf3KHNDClxkV9PtHLr8xzpKSax9Iuqvv0t6JalSRs2gei5AJgfKxnAdrwxyi0sqXjEO3zr5L"
            + "cuEZmbkrc9+O0w4SQtvpYFcOkWVuSemCkS0GY0hzKc9LeRVG10Ms/UVYZFWxBxJiT38ZZyF0DBJj55XAA5LmLQ3cRzH+znP5zA3T"
            + "tQoC6cJkQcNW8IiWfMirxyFv7lM5lXIQE01iyN04yftZ0lpxtxKEAR2gOeQ7vvkLSRqY8yb5v6VRhncmIlccDeMfrXTIaS54GK5j"
            + "vot0Fz+w/RgG+WF8ZqiMKHcVKCkHb0nwzO2NI+mvnokz13muqDijFrHIC636Dol1wdSKMlihiRL/Go2drF1rCRLlLFreJfp5qhc8"
            + "m2p+C4/jwvAhoxvKts5XNfHXEGNszmsUqaFrChuWRQqxJoAy1OYXPrz6n2aojAjB7Px4IV8yl2d/BrCtaxykD+6//SZfgJh+rj5a"
            + "GKpOeiDoIMjt3ELXMlfDahlAZw4YfWc2BO/ClTNz4jzKufhutQlNXCXIIxVm1xORYuJ/7eiqX/5LxGwgw7ZBZPBXZmIy0EWxrh95"
            + "WwM9LH7heAtgczvdMX9A10QTJnGqk2TOddCNeYuBG9dbpJjg2iRXn34oBkZ+AIwXeT283DDd8j8nDZqEHVeRQjm5dAmxXXZqGHcE"
            + "vkLkmtuT62opqAh4uTRklSdCzrzRbdGX+2kfbMxTGONm6UgypbmRkaW9ckp+wBwaJCWbrc0ivJpyJ3d1DVE3PmejihKodrLhGoOz"
            + "4Qp0mRGHpx2Sb8vb1vJNB5AJFajzgr3RfEh1znjs+h4gKOqHKYxWN92jHz/zR4B2T6ldQhCSRkavA9Nkjeb86DM4fBLP1Koeq0MD"
            + "HuIQSRhlElO/7uAuVw5uugix0FKDjX0eQPJJC2EWg3tInBUBpqhYVoUgdm4qLQgLeB3UOEGva77ZwNuOPFhpWhFNEaZeejQ2dmtZ"
            + "sKlgiPFzBIPw1HWqLCtFh3C0xCwDu0fVtve4gXHqsqS9jzmRrUQs1T2IEqfa6ryncZJvWrIEr4FKaxWjAMhxFZ+87a8r2eMRcylZ"
            + "JDb6wjhwQsTTyznJVcVdOjvcWSER/U6LVX78HC/te7BKLA6FRNStJGBmHy0QzTXJHeED1ZKz9ZLa6QRrFMHpUCc8TY1vYPYOfjFA"
            + "h31sqjQvt0DlmktbzDkGu9ob20kHg5s1VPFbFPcCVbNsdgcTvbn0o0o7E6RklbgTB7DEzfHiuQbTmvrFN8prbjKMBAsNOR2cSkzL"
            + "4fV844Tw1r++/xdBhwTU/HjX+CgKqHHwrQCD695KNOkDBgG4eE9VumOSPIwK28a7xh2ivu2S/eB1uUzivzkdXp0bUgabBEPlzo9L"
            + "BY0i4KkWd/0ECH1CBjiksCJKF9r5QD9KlcnmS3pVCEOqS90CZQ+nRZnxwfOFo8guJ1rQvf9ApsWYDMISLHL6AwJsfH+HUvy4hzNm"
            + "sk4vm9jgCjs9XEj5/HaKVBFhemriEM0CLKMHKuwvAPPuzZH0p6369qjrHfJJyKN3aJo2FjLZZh8EPJH90YAi42FhSlRUkYPAuWlD"
            + "fFUA6P+SlWx8zW8gSOBTtjIA562jikzjBlQgRrdM4ZPrJCblnarJKzQgK8DADafdG+bxN/8AUNWD1EUlMmDl7WCkTispRQGmoa61"
            + "iDM7n8b28nTqqg6hUvPRdlY5sY5rueyoXgRIyS+cuAoY4W4QdHQ6xp8N5mpAE54h19seAgCXYBE4tiH5kRueKV9SMovrY6UMOkzq"
            + "3X0BArj46+2PQsfnBRU7saemyRblhaDxnExeTlFszSAaM+BVMoJF5k7yyN96lYHO5ZUjt/Bbx2evSvcPjkqdavdsuQc7nkpLQnsl"
            + "6c5WkNSDKoHznyGmzvbNnYE/08kcQzPS6SzhOW/K/1KN7uIxvu3uFg7e9HSj2JuXnw5XmMiy6pFUNVHtPKnElXQznJqdeR9fno3+"
            + "+hBVJj/ufZe3HPj6RLLRAmHRsHJdq2mwi1GGIrqlkD/VxMmUC7Ufjq51U6NXvDZSB6aLts/e0hkVphD/4vGzbDAjbJHjOvjK6+OO"
            + "bsm4FRbE911i8jTuEPc/84PyrPaQxdhYeBlu5ptZB3872TbGvGdnqJLRhzmRICx2MtKOWp5BRnEFgi9IKzyax6nQQMphBqM28GUA"
            + "CMz/h4559Kpi/cBwCtuyTKe6K42My9DaYhuw5UKB6z5UrvMaqPli9r4rJbzmpcEj3yKat1EdVv5IU7zUSNxy+16h1OA9VNRJBdma"
            + "BQXtUld3r96i6A70/Lxjm13wXECa94wB33+dLQJaySHan6NtjLyhvxeHRBM6WSKffdKuN8J4wAT8mS/0o8vCdo/H5UHtwpQ7gs3d"
            + "KUjleQruBhIMf8Nh3t94F/Cq87Us0e7yihJJMg5n/4Yo2ixH0IbTxUm4Rh/wH2rUXPvogvBachdl/MzZgJfpJzQLblxTgZLmOHlP"
            + "fkdVf/ayjqLmxIpB8YcUv/9+pC/DCwATrNdkKgXMEJjJitnuvuQpnV3QOG/wNBq6gIfP9boCXrGhl1OsnADjJyWKeEQ7VCGytNKz"
            + "1PI6RWWsjQnpkqCtAQeaUbo6kCjNWnkaWgP/9dBx+F2XvK003M4islJV4G5pA4YIXqvRuFFoVJ7Hpd6oybJ4Ir1V204AigCxsjrD"
            + "9jOy4cWTNWDUMWXgHw70vkDqGC0gNZhpth3v2biSlCca/WLjJsHW4q83NMhi3eHx8qW1B1My6z1/zOfdJHO+IMf431B77W8EmMWP"
            + "MX/FiJm946WOIamSdT5NtNaz3I+bBpZt1Boi/As7EqM1JeGMfNC6/lxamSsoXH0Ogrryid70Rbti12mAyN5qLANI15FNe+ficAqS"
            + "4xJYsG0kirzyOTcYjq04NctO8fv74IIjwmtTpnC16Zo1z3hAf/+X29hgRm9TIjOIe6RnuCbIhwxye3NRvj6DwvuXoI7KMkA8uu8S"
            + "KlTQj4N1WYqcTmNXdMZKR+aSxRotHfb+je8oGHeTemjjT5keenjVKQa7m8cLgmzwRCNW3UIPIMND6IyMEIK1x/n7PXrpsTGJjm3w"
            + "V9NoVa2W9d+QEjL9kMpi0bKdpMAGmURb7Jjw+f2s+0vUfPjabHgUnAY0dxM8IJ87FH0sjBfGo6bFWpcip0q2W0OFqs3um2aoUeOJ"
            + "icBT/69YHmKXXSfa4SuHdWAlI7igJSxFyljgneZLh/wUd2yNOOD4VphK4bMMC7EQSZI+i51uBopdkRYgn/eIZwDjDAGEhAJ+4GoW"
            + "ytDQtNkj+hbctMwfiXvDHb8kEwlzi5pwJ6PF/t8Nhg+oBoaiGMrVUFtjBUuRA29seb3A6xKgya5eKe7D5OMo/O/BJ3O67tKGbuDK"
            + "bSY5qJ35Uyddv74sPdnI7CT8uMpPfnNiUgFUQAD0Vqxrr45ekDeIQTihQmVA+J7sxDtFme7KIzOSd/M+UQQoFpu6hVMdetG405t6"
            + "3VBRIXUQ5qTf30/Q4UklP97DbVo391b/3jiMZLoMlL7tkZRRb0rlNjsSnsBq9bMaBFcOP2Zhwjo42VDUw3iR6Qt4Edf9PDI1ee1R"
            + "rzKcVqGokO+7eOadqDT4AwMMIE8MX/w1PwaHm3UhYSGFVpmFuel8Kxt9DAyy7U1CxsxTi2CIJ6H5KnWkXpKYSFaOy7Ay86FtBJhE"
            + "6xIWrFWBAEDrbHeu4AZyCpI2VG1EuRi2WNTwoj17T6hxICPH7r2QlK085EiVeEscLdg0j27uRXp5ERvKjitYOHUNl0XgDZrOtS/+"
            + "lC1sPQADIAJhQkxLS7/O3nXw0J30ltcKKWWvGI7U9WdslZre54A9pVMowB0ZJ1WCF6OZoPB/2xuNprBgXK3yBqeg8dqfWiNxYpEQ"
            + "jsWrNQRh7qF7MQxDr/Ptxzs0QrKNN5SPKAa7IUPAvcktpgxi9J2FXltAdBpr0pm8LNEIgndxyo2JNcZg0Wc9AwGqevAVTxC7z4rI"
            + "slyNkUVTTAyFf9bOWrc25JjkiMRrH0pmAocaKTdCuDhY+L2+3xQq0nUQLecme5h4v6JQQaAI09hxA8IGIYfBlreQLg86m6+yHlqv"
            + "wf4E7H2gJSBHbUaKftwLNAdBXs7g2EIzaqJKhOZtxaTgvLJ/7JmtS74ZIop+qRCOoo5zb95B2rnlGwEZYY5a/EbEDnjiSWDQJs2N"
            + "dIvd1hP0TGomaJAaIQbZkdkUj/6+9mSsFvI75FRw0FYK9A09BzFQU0s+Xo1HddFFz4BMSU+OuYOdmT4dDjidqKxZwfKlKqxEtWKd"
            + "eh5LpA0Ji/bQ6/eU7WHl+798gmRGBXmYiskR8YT8i0DLOpjhJF3B5zEHhUQq+42fPVT1u/eXaQsAtAXU8n5BZzcgS6Af9FvG/B70"
            + "/lLBYNi81FwwgU96cW0ZymtfGghtUgy6yx+ghQbUXHG3yGpJvaR6ki6WLx5IVkTNxvCmFMQXnqL9qAOWveq3QEmKvVgrbuuRE3dK"
            + "P0E8EEape0ZYz4om04N3ABvy6cGIkLCbGi18KHapExm4NqBo3un/Bdn/B+gL+/EGVw13ryEH2OF7LVn3paJGEYOSlWcDG3TYLIWs"
            + "RJWCcF48tB4rp2d9PlZKtWhsTPWh+wTdOCaSInrWHO/gAdGR41Zvnbm1pDU4rANPAUUEwHmlyS0ciTALzEvUja6PpZmOCuGjuU+T"
            + "LfR6pQu0EOFIh+cM+w/Perc5rYdFddjzJfVNxivm5VNSRwm+9J28Rjf6Wf7tIsJmpDz5T07pAWGlt8+R2LtsC9nHGkhxhxoX/CMQ"
            + "1sB2VjWYgH2SYPHomGoLbJcOINfih41KWUcKreUX3O1twnEi2vD9fNB8Ev70WBkOak+WvzeZOt6Ra5tOBo/X3SyJ9wZpnsIvTAFI"
            + "sj3bpnicpRoJCQXZERTCX6ROZwLueEv4lSQ+6BsDh+fKZAyth4PkoXfTzEF5oizeS0lWe6/+mMU62NRFfwgKY9xuFKAJaWOZXFhi"
            + "rJZoGIT4m88QqJp602AvBrdvKKuEeD6sddtp8asImeERSBdQq2tlN0AeFUC0xfgsK/aD/oMDDL5npwFg25Y+YZCQMCtmT0KhWBce"
            + "GFS7YTjJ98ptbCO8Yxd8kcsXwDdN0JzORhPK+cx0D1pCQdOukz6eYMovmbAkux7LZiHQ2b9ZrgeL91bOd1S/LOf6yMllDG5jkThH"
            + "4HWnzvdwlMUySRGAudAnwuf4ZvHJj1dnwNKW9fi5RfCFKiM8yjVDfNHO4jrsw7N0/5N1x+kLMod7SBRDKCqTzIGYdmGZMXUv3tlF"
            + "BEXnxdjs4X5Ws3uW4EdHQlNx/HSBIoxvYmAjwEcuF5HWpAK4xiy5Efm1s8vjk+hUHeOgFCDXzojwO9UZWlr7rihvYyTb1hFxtkbc"
            + "0b4Nt/xLlZRCRgcbtemUyetikbtrA/rMjD1YstNNmnKUSAYkGG8bNGMlp1y36oRAs9iYcoD+jMUXFcHojccdT72cKmEqpllyuGUV"
            + "7m94FdRlO60a+WhiW9Rbx58XIV4pSmkT33db3b2v1GdHYlG5cgq4eW0koxMbD/DFMHQC8FZdswLYiGtOVqehe/B/b/pebW+/bY5G"
            + "mRgF5iJit3y7lvYYUqzh7zBsx86dC8WsgmP4pys4smQgdAMeIc/qJOhAMkrDMkuNm4RHyn9gSlaxUSx7DIHn6zHMGC0+QIEz6Va4"
            + "oEA24UtYWqbnPSuT0FF5WclkiuffHQ28iowyxS/+Dq6zefdUhUCEw0Fb6tS6PYF4bU9O1YvOXQA+XH/MbOoE9W3JDCdWbcqKi6Yk"
            + "Iqnh02085eVue0UpfjTjuTCsef+HGhQspOcdqHRXb8d410YJMCnPJS0DYSaBMv5r0l9Wbr35m2QT2iypWVfoRX0W54HNCJhBKk8m"
            + "ShuUq/vO3gUjkdtKCTJPbAKWuvWA1Z8pN8+MMXmAAEFVn0+ZazTj4B892dFT74tSuusjLA/C9qnTIe7YUKC46DJVFabw4MgxDCi8"
            + "EieglSzPyDwS++ebrNYnEPGC2hnmfW9zoPBPiZxNI2Wpz1q5lDGJJwPN/NcnZlcpJ2havf2uaO4NRfjtLDn6RmC5fioBHgZ3k+Yw"
            + "n6yHNFr0Ucv44udEhzHuFXPH4lvxScJ/A6VLDF/bmp3jxJ55iQnxttsQXhNMSCeOUaXXYtCDMPqTsDz5ozd9OUx5klkgNzfgI6xw"
            + "wbHY/85duEa5Hc3AiE6tOrxpFKO12LRun28Jl3IPbaTRucz7QCQ3dFn+73FxfL31rfl7DWI+PBaBzqPFAe9VYUA6182LymXTadC/"
            + "mvtovDLatFaSCarO+qyzg6sV4Zw1ywGBEFhZJQ5gwjGyaeWwU9SO6eotMSnPR6p15WJnEZV7OCiCQmPiKn9Z2o8aHz01H32trL/h"
            + "hXgTrisyLpVZ3EngS1Qqmv+lW9z9uDXcLIYGduY95TxbA/lhuidS4FvTrG3MCedZ2TERBC4PK3HUFNRhl/cHy/XXO4v84z1AM9xI"
            + "RIdLMrL9pCYBcUqyEl46+KMrtmiD2SVcte63EZxPgUcuffDznxKxMYg6kRUEt/ylLpy0mtFEEitQUnJtXK9Qf4DYayTHDnRlIVSV"
            + "ZQhu1SoAa47x2tw+Y5lmgnoyRM+75xSJk3Oznvd3");

    private SecureRandom random;

    @Override
    public SecureRandom get()
    {
        return random;
    }

    @Override
    public void initialize() throws InitializationException
    {
        random = new FixedSecureRandom(RANDOM_DATA);
    }

    public void setRandomSource(SecureRandom source) {
        random = source;
    }
}
