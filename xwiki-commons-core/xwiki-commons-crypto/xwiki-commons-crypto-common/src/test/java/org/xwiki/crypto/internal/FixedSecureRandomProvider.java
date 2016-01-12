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
        "zUurWfUUIWxZ7uiYtxOY+emVmTgjukS61doC6zajs0Sx4kFjHrXuG4TBzeuhJk5HuJciPBSBam7w0yrWsertltm7kNHjvmnb6yZo"
            + "QFWqkDKRvO7GGsocS5Xu6kYmK4Y2ceW0C2TQkAYGevgc8duX6sShak1ujkB5DkWHTPJy9lOOdVIaznUu4UZUQXGJFUiNCqxDksHh"
            + "GX09RmqMtX1YgY15PRnrSxaTYGx9TUo1qTK7somGgJKPVvN+lj9tUKiwHmEXXf7Hudv5Xu/d/0rsGHgUPnZmwdRHPxxl+jPv747q"
            + "/7ahyo38E4YBYVP8jx1lT9xWieCuTolKmRuNaLh4/CZ4BGbdER615ELFye1VINbySq4btzAmgeTo8ko/e/zr2E7zuAMmMRJuCJM/"
            + "qQ/tEssDGohdvfhG0E234nUmAwoji0Q/HFstcDYPExjL90j5qObKMCIsmTwAvyKUkMua7jYMmxjAJKS3eaPYPo8r+Q87LQtkxcV7"
            + "e14mazvDgPt1Nd9riOjOKsoaGHpDf/nNRU9mysZXs1UYxRrfcrjutZ3N/w2cip3edxVd9hI+YoB2/lXcHu+aRDh3f8OSJCib2MMn"
            + "/mi0F60YQwXmwn8+ZYssuVT0CC27QnsJDBUaV/MjP1B0Htyx6Zajhz9PrkVJMAXY7MuRDg7tb/8U//pnu3GtXCN8DCLPJ1plBPMS"
            + "z1Vy7LtRCyD9jcw/5wDIlEBBP69XgRuVlvMI5USzxW8hcql3h/hgkPC4AOKBq1P5NE99dxaTnUJO8IeN08lKq4SIG6+vtdbJcefP"
            + "Fe5FNmwE6OQv0D8eubz1gQ7QCr4knObcqpyC9gey+2iRR39ituJqESAj21I6g2rxLI6C/tEBYSPxo919toNvbg/hX9duWDmjHYtt"
            + "Bj3ZW6BuIc23GGbd+XiISFxrXnuLOq54adE7XXF2hkBl6xlz8iYu8FmXXtRdOqekG+OH+HMJ54KFBr2zYCrAX/3iUOKdPgDCVt85"
            + "/94hjfVeBw9RrWtnSqvbqILczaUUzI8h6z/2QFdoP0G/bRJhMT0EqluafZexrInC7Nu/K7eWLkqFvCAnVG2g+MGGpA7jJ0j7gb2b"
            + "/Fypiv8qBC5GERxpqx+0mjjusk4Dt75KYj4jGao+2/Lx2h9Rj2EvJwDO4MdRW2wjoknWG+yRGJ5mMUfwnQjxQegK41HiUwjjXLEf"
            + "jxSbsocd0mMi35SuxHLkYQm60RgXoGATJo1aB+zNEz4qz++9ROm2rsuQ5GTPj+EHhXDSw9fMJ/iLr4eER1Rgxfy0QHbhDCSs6OYm"
            + "YhGc+j7X8De0bhNJ5wNdVI9xwSLLYyE2F2pEzCxCXwsozUR0A/aqNG1ETh+7Kn2xL1D7qukAm0lfl5V0Yr4TW4ZveFDUbAPeOFvi"
            + "xB/BquUyg2cbvW+aKxi85erXIEjw2n++N9eY6InYCDjrpFc6CQYpjJ/ABu4swBoig4gdrCC6+F23M4IDec+appxHgJPJwiLh/vYm"
            + "coCTYmZoYe0sZamUcRIewUrUyw6c4mmeAyn4NBq0a9JnsZA/d9Jf2d+JnPYiQDWHtQyVQhN9sStR0LYoww1dFS8eH9weVZ8TaJBe"
            + "q+dD6uCkC+L/lwVwpmO+nSy9SFcdvEZCr6YB3LN9jsy5MvPW6hgbWkwzWjVtsOKA+2Ta42mmbSn+yYyWEgu8h+vF0kzqq+GPxF2x"
            + "AF3x4YsbqxMFIPFD5mRF43dwO5dfyHqmhmOB8awj2Fxo0zcVfxySd+1KictbGM2fOCUP77XfYRiKqso8w5A7f5nWAH977/TrwV2d"
            + "tNNP7RRDM2xM/R4GuIjTiN2ZBw454jPly1G5tIDld77RXBMrfWSO2ePkJ5Bjftwy4SGkD/vGpU+OKkSvRPX9fgKYKMz5Bkfx0JYF"
            + "48A36bg0N5ZMyt9pQSgp8t2P5bm5t/Kqad+DpzRKv0T032jpyIauPWhDKeTIVYUVXzG2gty26Wj1NhHCAsDBdFB0sdcjfma/BNab"
            + "Zq28OwfrczOXMMWqHXsWZ6o3e8K35EVedXx8faFQCKCVW1z4a97dEHKsZsKVSuMft4Iz9ENVLLQNNCOhidpIf7kRPzq4lSWGxtDw"
            + "FlpRHOjqLh43WShVQ3chPGWoAdmfA6BG8BOkPeFYtSHP60y1CXpsUTvhHJKB2Q8P0Sh+mCukjlKjILXJh0t8yQQtPsx7tyqbfxjR"
            + "oQh9AtrhKeYJzVSb3A6LzfAbhi/tw4TRzF5jrhZRXNaKsnLmpPjo0FKoaqzQtQQEAEslFSasDdK0KSxZt6Df7C+4uUpOeGjjoAVM"
            + "BxAsU6tAXgwwtztkAH1vL3saGVE1qA8xIrRWZjfUnI8RutfV4rEnNJZbWYJbYyw4vxMGA66Iois+wM9CMy8CPO3mJ3aAiH40lBFJ"
            + "53SYaqwRlb2Q0yaasMrm6VsT6ikDtbMA7BC/g5uHQB67dLmKIbtoHZmWpiUqg+QwckOLVQVC1cISp8E+Zn7oLU/oygLQyyWYH2yD"
            + "2aV+h+C3NIE0f6ybMDIJjAT6H/dmuUiXMFD+U54fveYVoAbTAOoF+UyLrsP9CKz9mT02UOpWGSj4A02OHLLpzaiPmWLD9vlArX1h"
            + "S0KSEjSsZCVyLCCqOq8Yv8qURMoHdpQmNf9Gf7INJzlNvAADEZhdcTtFsyrS3dIR2YkZ5ldZ4DduHtapGFSobdRcDZSy3qvk+dtm"
            + "kF2IxwhlJc5wtEktbC7SAVztlrPWOHXdE5vcUvcHGJV1/zZ6z64LHz7qkjaFmemVM2nIUpI6XevOSrdveOgpKUuBlq2IybG3sp+g"
            + "iVS/Tj8oHiyz0DH1i+VzG1N20HO+jWPq2TCoej/JFnL0l07q4DTNRemLLuQhPggmK6ldrBO59NivF71v1M5XAp5wBF5SImJjCLyq"
            + "yBmqeqUBjjAblejsoc8ofdFs/x5ZCQPq4zXcABPRX5qqX1Duj7JpiCJuC1ABNohsrEb+Y4VaL81VFd53Q79jIdqIyWr7OokenRaG"
            + "F544HxOxT8EL+D5RmpBfZBQ+9Kr/EEiDZP+IReOByBDJPETE0Z4xED4Lodq567muGwGgtVUl8hQBfayJi7eGr1uQfgftFvSAL7G2"
            + "NER8AczBZ+VQ+iUFo70V3kEBA/HTrEyM+Nmm+NgiTMczBV1HyenYJBNQf/TSUaTBF1isAF1a9KStb/ir2qFQNZCLosv9NENGEKgF"
            + "wCMf4o8wexa9FIT0RXwfLa74uQhm/L2CwwBF0dnFQXKs8BZvWCrh5SU9qP+BEEPS96NxbtCvYvnFCodHAbzps9UDm2rAriOzQCdU"
            + "cdx3k02UU/HSb8k8CfKzii+HzU00f3FX+RMb03ACDGW+d5zvxroqsqClqhpUL7ViRn65D3StN9xLDlNM6SQWoKfUcuLu/Myxc+e+"
            + "yNcqgEVoiVn1yvzg7bNfTRzt1+eZHdAH/Ev9mhJljil8KgFfmuk4WZkTr49kpJB5N3BYxLU0Tka4JvA8EqfyQQcnVe44iU7Nbx2w"
            + "xIuDcq2QtkIA9J9nPIAXvreZvVCZz3hl3xlxMO4ImfKuurhDgrXsrdF9SulmdEZ8Ryz3hz1fECktX1oY0d6kI/zPJj5/0Pj0kb2y"
            + "GMDochofJefVYokpy6fhXLWgyY6gF2Yv2gI6kGnZEk5xOGd/SSLome+Y5FUY4/V+vANB7tj9u8ZyOBBgu5HM+mx6gbTapmvA8fWj"
            + "bYFzXCjAj/KuwEvVqK/97abg3T9FCRmKMOCPzLncoBjdRDFFusiv5mWkOZQNqQx9zVqpoBEqEXmXoe51QOxv/P3Lu6bNFU7PTs50"
            + "eN2Z5qOj2yK9Us7eTW+9xkHjIqvSUuIrFocZ+pAdjT4Vie4oA5kj8KTWPI1eBaJP8794UDqrLmMravru4rrGN3xmtNEVoVe/cBd8"
            + "X2ulZCbrmEcnhGBcTHrMvO2HDFfA4HmleNDxss8ffUQmEVSjmnVoJSqMdYRgxG9L3lwrEfSYPHYzUdQAN+3vGWftfsdITYIKTq9q"
            + "mBtpGVPm4/BF+TJ7kmvCerY4Jkv4xkuRBAoKKsq5QnPB7oBuvK8D2ykPR5XnfyWncdC03Dvbg6+RzZ6U3NZPqrO8126OjHZmL+Tg"
            + "QS6dV/0XUutDjLMx5GwzeuPmgESM2TAtOwPPqINb0hQ0HTOc3STZLlRaE+57oAGrngaKbj2xWO9346mBEVVPIPqeTFPFcmbD521L"
            + "6WtAnPPiPhOZYwy/GeUF0jHIKMmx80Xbrg7/YTEFuDzy6r9ZdkqAHLd2DgFZMi03gcNc3g+mIrKvxo0iGw8n4W4FlnQ+dfyg5m0H"
            + "NNxdav2hS0apzdLeiq7xklovGMyX5N6zHQxYSRSGNAldgIc46VL1QILqtifdrEadr4j1FB1i31vikSUMvLVZENcuXDnJqd5/wLoM"
            + "0Xxy9KzJOPQaVgvY2xhnbAng9ozAyKHvoGGak/Z8zRvNSwVkMA0bMhPAKziwVM6iPVVCIoxoplXy8JOf9BfoF94D4ueptqMZhiBJ"
            + "1FnG3R58IsfHTTAqUxzmzr1BO80vgAQrW/GPkE4nXpSxNjB57cvBI1IccfZwNBbATL7yF3729nwKIZR++oh3tJazGc9RicTUZYtQ"
            + "osyvWSyeYFYm4TjoKwM1Rp6FDnDiRxesBo1gy3WJ71wIakoNAib7uncfPXi2SLgvCB/cUUt7ZwowDDFguHhlkEhnWVHa+YzHvLYy"
            + "QXs2ZWdNFFYHaNCrbhEXsDkzNex2G/hj9/BPAay5rcFq8y6DwTIlLJ3RpTNwDZVOQKThdaVLTuu+hrxyMJKY3edPsEPq68pGozSD"
            + "o1/6gHKnYo5AmYMQsQLnAxHELXrSj57Vjm+lzpQnKZf8XaTVXk2dARPiY08AZMKtgNj9eyRLSIdm2HHeprhW5V455tXz5CobTp46"
            + "qUsnbeQc0VnZqlI92BRJuIjwvxGrT+k07c4WtBFzsKPChK1tIOiqIIgQOddAv6x+bnc24IXbqn1Qmz9dLhP1LSvmZCW5JyjMAwAD"
            + "ewJWrxBUQvqFzwVPP4Kji4vOsvpj6hnCN9LOMUwjCFewpZWi6xvh8VmmZ/ZI4q4sXBT23OCIQUZ7AN5AwPbb+A/Dhs8Bp6CiPy4F"
            + "iAn+H8UL9DQVKplTkAx9LsqIWSALdV7ZWgOu5GJZ9MqQ+MvaKLGecHplBlUYJMwM1u520nSNyQxf34aOvHCIWam+5NUsyI/ixe5L"
            + "POLET7lVZSmpFM1MiBRBs3C8/VaX3314CYeEErorRug2TpOgI81O7N9CIZIdHBbtLGKGD8pYSjrccpWUrm8a9AcWtsXIVXMljFcw"
            + "Gw7Km1GjD9PHVgNbc9o5pFo5rVkk4mknVKhcG281NEWtzeRWw2u2FOhfZuPebWBwG7iyqtogjj6jW2hLubERDWLpoeHgz6wIW3Y5"
            + "phrHpd/XHKZiy6x0BFV1jN/M6djlmqox7mQ9ZXOPZL+Wjh3xfV+39LzsN7+lbSGIl5BdW+rN99MC5lsgzhE6VN9ctOtqN5C6UOlB"
            + "ow1eE8cuC4bIMOBuJXpl/KQojbGWXRaEihAoLZBB6Sj6tapcFH+gYpQKcNjdmnCZd1+zPQ1Iiq0p/ls0RrqmI6St1EcpGGXeolaA"
            + "IlGPoi6MRaTGHHtRWtKHSgx6i6ezeT8sBLrFKO9Q3P44De3d3AjNa4V6XuC+ReP9uWE3YaWPEgCjygjinH9NcQ4sFosLMXFn8UC2"
            + "5PHCnPb8hW3LRgg+6+Lscau+b1teC6fWHHgJHoW6AVtg4hxHyiPI1YCN2M6dpg76owC55p8IphX2EhXP8xQmzttui8fQKE1YbPLx"
            + "BcWLlvtMdNe1T52utRE387Uh2MbIMVKN41okT39fXl2VMK8icWT2BF1Rxb+XaHW4EEehJAhq0HwvI+HPQOINL/XocRGsIuJGGxXF"
            + "0p/ekORavO0y4AdHiVKT1fPeEDNZVRWjfGRXU8KDZPchGnhhKcBbiQsNEURBVD9Q1Hd3D+c81R611+AQxvSkeaMMxFI/wSWWiiuo"
            + "uYB9WQgxVs2hE/2sjH+T6ulRViI4q6J00pAsCSs46f/UknzNIO04cniQR+Rbn1PQn6REgKok1rLN7NazfYRwD7uyRjQjEdPlCZN3"
            + "cU6gDSUcQba+h2BMlzdHetqJ7MEozDSmJK36StV4ArUnzMxmABP3/+XW9f1uzuk0giJDSZTsyt5M3vZuqmvMnshqK03fFOXvMLrb"
            + "eg/HQMqhuWSstL37Q/knrfTR1kKBZvBgVRCZuG4cO6w1FcT8p0xkcjXs21fXNWzLLT6ew41o3Gs2TxgNXc0BodGv8UBkpGr6+Pnj"
            + "BWReR0P5gE2GLz4whSzYIgwrioZae1ispkJAdP/zmw97M87bUCG1uHp/1nkcISNEsf3a+zXXSeWilRLaqFC8ewtcwXrla4bep7tp"
            + "GcJueMHUDh5KdPZJIdFwQ7F9Ge2DCzbIKSdbLhNJbysgv7pEIw9ICchiSz9GWcIT93uFHDVwfhAkuIt+XDOMV0u59/F5Z4C+Ayf3"
            + "3xgmbmdXADfSMbzmXY5M0kse1yfPP1T4cuZ2ONawQY87Uz8nO3JV6kvRxSjaHybOO1XmXnwNE42pdY/0RfAy4tLR7EjBNA1jCVWT"
            + "QXw1blwH/XE7JO+1QXlovX6ABwvlvTFZ8L02G/tNP7FjJjDSYw4CgvTSL60NuzuuwpGLytW+5MsUczuEXVEfJP74e/rHub+c15Un"
            + "wUL2eyT1wQnumMIl01CLesfbKlR6TnzLkXWDtDiIjUbve76cyxb7VUdha+g1Mg5DZKNo6e0vh0dVEPpTmM4+n0sGWzYE185B0Tn6"
            + "LAgpDHPycb0f/TRQttBuaCgPgvPZKKzf5fnabMpmh+BzGam9oMwJ30OUY22ImfyOLh3ff20CjhO6dZzptR012CM7DCwa9a5zjP3p"
            + "xD97IJjJAyAZdRU7usioj2aKMs8JV5DH0fVWuLhcMJeelAyX27zGcpYGpz2oHaJnSfNtQfR5GV1dRTGBngikdaYLj77FrLDlKTBC"
            + "7Fwi2xv28YnB06B4ElTSTH6+d5b8hUD1rFLq89tbfbG7KU7nQgakDRZTjM2CGoBMpoCoMcz8vxeK3JwS0t7QL9Aa4mlobD+twmS1"
            + "XO1SAJBwYK3RvUD/zViuEumi12pAV9yCx+jwQOF2Y54ncvzU1qm1q8bY7MXU5dyZURDho7U3pJhxCF8BUw2/1jumP3AtNJr/qdhc"
            + "KALhviBL5PKO2U15t+b8QDx8vMjb0eSFRCxLeja+AbJEoN3jsMmrxWKelea7WJXJaVDR1ISwVa4F3xxYJzo+ytOoqY/gF46HW+bf"
            + "TDBh//Rih55HKyAsF3KBmht5ETs1yCKSAiNrNgack4/cxfIvxRTQEtrXtrlXdNDZ2j5l8FzgB9XnNm/FZJCOVe3MOUKsXltPaRSu"
            + "Gyt5tAHkjg/ndnVDQqMtqpIr9A+3PdWSxDX78tDl/AenQpgm+9UZJvk62ClqrlKo9PIuhEYUdHZdQLm6fPsmn2vyx58Sv61BKJFJ"
            + "pD0ca8sdzFpAi7BQkdbDe7Lghpt6Xqcg7Dc3SlEhxzVsADU1tkW15l1p2bpxp722cwzjD0suSET8hvQAp8BKsjcxGtMPm7f5WmTT"
            + "kF7z7p65Z+JT/yLBat9AR27xIcHaiGFg9vj/j/eIlddTUgJ+oX8t9a22ulb/a16NEseOYnPh7stYeA9y/YTdvhFH092T2j3cb7EC"
            + "xUeCPYMG4n/JTq/XI5S2fmX7rYCUWqKonQddDRTw0hJZ6O2Mvb+/mQgk7DW1pbfPqe/QThfGg/wSvLd0tECV0Vl/Z9iPVTzdpdWQ"
            + "D3V3DAt1DpYWd6LQTnaRguPIRbVSh9NyEtEhT2qTyhVNpJMOp434enrpF+aNZguLAlQFtpCfON6ny+pL6uvbF6HWajXIDPd+5Kam"
            + "GltoZur84jCU7zz1fh+WpITV+BgfrJ/WZUwdCaoNY3uT1ICKlAgeURXZqaISvxMqHegLFw+1L6W87VXzjdVQLExJfQbFTfcPDeeX"
            + "2tJCX4e6QA3yetkG2ASV3/1kOS04FoERhODoSRyBxS48CWHDZP3H17j+XPghHVqEBXC3U3BHuy5+L+vm8h8IX3V5FBLzkS149s22"
            + "+XQx3sbaaAEzfzaNqsRW6XC7t/wtM8ZhbhCVmOeoSUL73HUdso+5bwz716P86dHb7Iv/aOm5NoyelYGv0SxnclHPRu2bxpO/6ydX"
            + "Y5EX+xA6W6AMvw3TIiYYFCokkJ8hNLnbHwu7Pw7/sX5w3TLk7TcxvsmwZWIoIVAjMTsHH0dTxxnTml7wAbT/FASJ5xwWL469enF1"
            + "Rim7yhPwUyORMdst15bGoCw20kVAmfGnPZ9frQhV5rN8/3SR/z8THvIB3SnGQe1AchRYw36osbLfSRyavnDmKj5Oi1DvLW72mtJs"
            + "02X9qVF8WKBOkJUJd0P2/pJz2H1wOsIu7ojfjAgX+fHkt09U6YgAq1AYls0vssbiSjQyqTyEIHzlyOf+xHWDiINjPxBCtQENoVfl"
            + "uzDv/y2UNrYbAPmuZa1ST0HZrQHSD/Cu7KAtE648Y+ou/nTcLl5YCe6jBwFcnkqRa1yrPEXCRSF2276eN1/c5At/QIVtDJsra5Od"
            + "7MLBZnn76ojZnpm5hYVHDTPSKqtngbU0MxP4EwdK1OMnlrcLyOCr4KYonDFebTndDOm5/pwcmBG96fqgUfWG0t0RlqE6tq3Nxo4J"
            + "zJPaihsOM15d7W+XohopK8z/YD4nDBlMQCW/j38LTJGcSUmqKjjkuRi7IMy4aXr6h0GOJ4pIqat4SYF4lEKu6trTZOAL72Jno1KG"
            + "H9vDkif0m3dumnGAD9KBXFEUBWglCSlhhVN5XBQOkq9eJ8SRD4FKh9qsEu3/Oia3UI1wGTcyv7v6JuhvISh5GjuELN79ccfkwBbH"
            + "BZrNUq5AEJ/365Y8Hvk8iU94KgmTC28bvc8x7yEDTSA4yurpgnkhhu0THsP2TQLphuRleeXdjuedft09HyG1WhJVJH1/beglfyaU"
            + "0rWBWAQX/He/7F6XMt1JZLxzUXxEiigJo25MngdwTp44Mr2RPddSUAQwa9WgUfK7u3jyh8OqrtpbN0U0hdilkMYPNj+SV1wnU7Mo"
            + "A2KkeOBxnT6n5/4l3R30RPiQvzwuZz4+0JlknYaiQidTQoEUdY89HdytoTT8sG6ASbUTlrmJ2epIia0fhZ9f8GIvuy6Zni0NqshH"
            + "BbYs5Bx+449kA58xYjMjq8R9EZUPuIFICIqTQqyZYVJY75OufqWf2nR97hWBcQtBkz/fLFZB6Ut1Yr6lBBqblqzCxPsrDVUQEULw"
            + "skiIhUBiqwT26EITB9xLiLX5tSQd5gNjz5tvMtmndDQawB448hs1Lui2Fs1r4s/c/8m/9i+kOe3O+TbJbQIin2bt8fjT9W2Oh46W"
            + "roShh1cQ3fcOa998kThPjVsD9nEjUgNbAVSa+Hky97NgvEcdVas/saZO81FSwnruq0lTZql8oFB7VMdAmjorW9YgQkcKt4QmlTTf"
            + "mc7Y20yMJEIALYvTJ6XYhhyVnp0D0/PEM1DE9SThNCWzf/maBjRrMkW1iM3loGrTpWVkRFTvYFb/cjiDYV9PsVMHYJ2khSJjbhJX"
            + "o77nVFuOpwp1VlvVKmeMntLcbWjuty3m3gRalbkRfnmC313N1pXu+4Co94Vs+xdcb2gufZjJ9QyNUMaIraq9xVWsoaRqWquJZ94C"
            + "cJ6atu92Oxw3ZmlePvCuJK4E9W2QRYsn7dEzB32kBuRZQbURcFeBPTmUs0N9y8rlvRAbswv7vITUq/u4Z3/muP2JHGm7mq05WYVw"
            + "ttWgslceAmeUgEyvNnd36cnc5NJ41qsb5Uix8cpxT+VywO3BPs9UJE2QTDFo6aQsYRUfXL+kNIMn6BkFSUkLyObTnn/ULfHp2maN"
            + "6bSlSu8AmvLVIaHaEybZOaeVzMXWg3k0YGeFoqqyb7S+OqiD/OheVFqEu2i0vYjD6slJ9xr+DVjQ6SqeEThps+bRlDAoS/l/sFTL"
            + "6Sq2UiF3k40t4xhbFGwPgvzYqliBcFdUfi+HO38GDOZnYD1NQmyQUUCRorBKS9lMqCdZ8Br7miK2jWVBDWLdEM7QTDNeDg2K6gim"
            + "+LXIBW1cWjRTW0aDknjsGRVX2VF7WcZvNtsZZFb+49b64ikZyBmftMjUijGsYSU3d+bz6IOQ1NXFNewwgkdWJJqDkZJ0TA8UdQP5"
            + "1BNq+ICO0DnrUh3kbCHFessxIXZUuduHdtEYfF0weioqOPvqV3E9bg6cXpDMX7CZduMaJewNF4RljVTw3fptmTccGqYJ1zlSGzmW"
            + "eIdpQzDKnpGcarsRV23QbJ77W+IQzPiCfATxYWadcOD10dU6cBDAvFX6BiAVL7LjNVe6vfhVjcn0S21K3iBzODIrA7+jeKQXnGBM"
            + "DO1XcYUwgY2x5JqkjAuiTzqrcRZvqF9bYfT6c8o1wvviXuKcDuxLgifc5sZYd9ATSdc9gIvBysfHyJkB42WqOVJdaX+jKRMRKN33"
            + "mmER8/5xo9iL9LjC21EUXte4MxP7UtP2mcaHRlj3e/Bq8W49EU1bkXl/t8wM92om84eUDcukD5WrZS3HId5VrP0l76V3J4B2F06A"
            + "Q0KAeND6y5N8fcGsJ4LdrBGsgwRjT38t1ds6OEu6X/QG5hC6/Zd6PTqzxGe7yKb16vnNGjKPHUvna7Ifl7n68kZ3wwGmVqgDy+ql"
            + "Y6ZwI91ztVYwIFsWfagUMF3fiKYB8HI3yW9x/4TCQjnLeUYDB2oasAz+fhghPJnXPMJenQVwUe4DWFWhkvWjDrV95cDnhyB8Xza3"
            + "HLc0RcMgh2XJbaiqy2yenCTPTmshFSUTr0Y0YOnbAuJBMofFrgV3tgoGIrHMoZI4WqHydU8eFcoi3+QnfCNQ6AoewOmUGbNJyWlE"
            + "1MZUFQ1KVJsSzr+FMsny6VFLQD2e0SZrfmTBnaajMCf8U2DXIHK/3rx4HUH5FB7oNbY+TTT460P+8JEY57zB0R79ZfUJT+FRaRGd"
            + "0vk6VURF+W82rNDC0lIj9bRYOcjn7bJOIIRF7h2OtOkP4JcQfDDnK1Ys7Oo7fnqzEZ+TDmuJybiLou273sofYt641qevC9wsTe+8"
            + "dIYnHmC5gfm6jDEMncEnKD3PGH+WJCJk3OYuCDeh8kVdg7yJUehgIaWI7zzx3AXBq1NLQH79xbsffBsqpN0y2sDnOBXHQapfYRZK"
            + "Myg8GV8nQ8zWeGrV8r8BZmEzS3tAVLUMSu4+ENDHEzyPGQuV8iSCWXRcIo64h143TJttMm2zisfgHB7exSzT4T27MGhjZsfzssHo"
            + "GL6SoThGuqjeDg4P5VtVpDgMWiRZase13WVFnTbuO0b3ludxUoSTzv2nEp5OmBY7iA2tx4xTClWjCyj/DzoN6W7wls9zOry4CX5v"
            + "ZgEMCcZrQrPG7QMSWxG+U7yoglwduUH4TfLRkA6f2HLXkp/VHrZc1yT8qvfVJy2o6QPsytWbQlVo4n0VeTvLnQdtIjaY5P+ewil9"
            + "9eI7cvUG0z80cVRpHlB0ATHumx7VfnKFnb+3Y/o18lqQP+Az2TbIPjUqH92XUFc7AI8pnHHCajlOIbnr4nMb8y5BjcyakkLizQPg"
            + "6lxtPZdFX5bESDAcGsqqNS6q8TQee792xrj0LckZN9dA2Wd4/yRYHInpPmjj8kn1f3HeLT8hU/cYRt68QglF/QvIBY/a4DQf938v"
            + "XVKCApf2dNdwfsawV6C9xWvAfO6tUvxtqoNjZ7pgAuMSr9pimPkkfyhtUdqJi4Rt6Eb2BOGkhssP6P1wpPZlPV2VacVVP6OP4o0x"
            + "bbeFPubfAwzc7uhrEGUYgvFPQCBY5e13IRvLBX06nFwOKztN/LRsBpXjMaJKQsGxj6oe8qkA160Zjxr1Nb+LBMvwCPGpHB3Z5xlp"
            + "mpyEbXISsSNfZuNi8k6XWKJ7M61ntGumyarEvMRbq78BMlQ13zSMbml7Aasu8qgSuDJOAqLpyoEz6rBMtOwXfLQgCq1MsJu0Kpg0"
            + "r6v+DGPWHQotarxcMuoaAQJddtg0OaxPtccoPppUqXcwvBCIJO3icK6bIy9a39AvFkUK7f/+iGxJcH+W9cCyiWGsmkvnDTnQj/qw"
            + "0BZkI0o8+j5qOvQc219VD84Ga630SK1HOlxsbd3U96VQkBQxi1/ouY7qrFVzFak3o9PuEwfES1W5j2U0tixbi2HpDbHf22lcaSfG"
            + "3z5TtneHch6f6T9u/ehKML440oTEH6odTVSNfKNqcWFGiO0f+NiNWAe7YnHawp12QBb8gO23LCWtyopuX/KGouw7O0SHdWuzlxMm"
            + "+kYZpGt/fprCG2iKHZTIwbEDGd3ZszR4D0jD0lkcaEfa1w9ClRTlmOLJHHbQ3WAHmLUrkWwBQ6jk7FBkpqMZRoyeh+OPq41XXegI"
            + "XJBgyfN5pUsGoAxe++heNp9xqoc2t3Fop4qvF6ScTUMuwqn0TwSr/PUJzGyg+Z1jCzJPyEwpNUVGCzEv8kBWcPjRMfkxfuDtnYwy"
            + "XyXJAqAfnED46xZct5c9w6wwm5INREVWeKM0YnEQGU5P3TYAZAwXsp6eI7BlzMrTNwE4AiBzaagZ7O67O702z/5YEa8Z/LAI4Wwa"
            + "9X0IWSm+cj9gIFnqyXUt5IMStnneyYAtAdumcZH+7G2nonkkF+p0Fys/HCYVGlur5w0GFw14Gg+WO8QfZOpZBN9EnL4abx+MP2EA"
            + "S+pOHxcbVkHOzcHjo/Pp595XJbMFrF5Ov/jTob2pQWwuVw44sSQxGZYBzZw+mivaa46LloE8IUC4V1XER9VzBtiT3Uok//hcDF0g"
            + "pTHnLE5tw+aWlTvYwh1uC6gtesclUF6D+hkwQ6OyFDIxy86rEFCcUn6ruQFrC3KC2aZljEhvn28qmMAnYxl2l9/UXSANhCpFq6Dt"
            + "njr4shcPv8RixDmQYBDgAnms0SDxAnkfsJPGxPt/IKTP4RV2MaK/c/2vWRSyG4IyfOFK+i/hdA5UYotsphUjF7RAZLoJoajaX9H2"
            + "vwudNWmkMEMQniM+soggAV5dOoUmVD+Cg733G81Bj41jJgQvADqsChwXNl4/szTrip8v4/P6Ho6CNqMPDVv2DSh6LKaSPL+AEx1l"
            + "L7FfY7NrS3uajLGmcsvZvZlOY1yJupSbmwVL+7WBuMTZ/PuZ2sGuG6Ej+l5et12r0tFqstbbHShvK58SNzOvqhlMuJhTlJVdMuiZ"
            + "cTxOfITEaYR89xC4cRqBeVZ4cle8WeCn3hoaOpseWPCpanoVycql+XmmbwZD30KilHdddcGZTaOY8yx9QITDlTJqUQm7O8t4c3bT"
            + "dd/Cvs0ROUUqlwTMPkH7OsGTOfuCbsavlRIDXMJj5qmMLH2p8pIznwTiMCPWvghdiJ/t7jOAh+MhplZR3p+3Jl1zgD8dpQOgy8yn"
            + "1vVPjq1OsOwpr90sgbcg+IiCEqHtQGvSM+GADXW8Q4yqM7+XhLT96mBdg3V7u24OzYtpWB4hygUIiuGE/itrj2RTRoKpKY9dJprO"
            + "dSuNnOmQRNe+oTfUwtQCDKkVqLZX89wVDzzcKyM6OGzuGCFdEdRkstE2Tt4VGLfbrGs0j9YiKjceA8kHGNyFKwVFvYd/t8NjNgVL"
            + "DqQWUA6SpT54zekaAlMrCDrfZtuXQB+15E+HWjvCijX3d18zzTbRIF9kmyFI8ZzRCd232QBx4WoI9I2ntweLHBC8cSGQpui2q0wU"
            + "PpmwlOLsIL/AJtEsq38kC70uxbrk8eQWhGduIW/1/Sd3G5+cdnMQ2jz9uRp5NSxzsy7EMdRp8Kps+nb3H+/IXesJT2kEIgB4/r1Z"
            + "FeJVVYue/zIKY1dzf12J5BEzqXIw9sjw6AkgI4tfqEe3j0ApAuBBgwf1A7igXWhnF8ZHbQhZUEJhsr1ROfDT/XYtGW396T4P8uK7"
            + "TgDlECsyliPGGZHjsbQfO6nlysKANGIeQ/pRf/hZ1PkGIiCj2d8873TlkgYpXCUQRk1KzFrpRSAhEczRjhwEKNtLuViQeyC7g2g3"
            + "YqFqBP46pd/z4C3pzN2HxOAPqBEhFFa9Oyc8RO8T+yz1NAHUFnRnfqi5NKgYAp4NBB8W2oQUJWLy8HZenNvntSXxnay21lPrKP05"
            + "sDnSJMlzsPhzN95v+iH4Q06NoPfGydUY8L1bZjuJY1MUIdZvDesmWSXDP20b0qzoMi9aURkDaGeCt3gwf1TIPKwu0AR95vplMhrE"
            + "HU3EjQVLpFES58Jnel10O4dPtzpYFlkX3Ieqndlq584bIFWdy047fYYcvBWf2djs2LrxqXH9XUxYA8m/1+1FuC+4mb5gsdSb7kFU"
            + "ftRKbTZ6R6RQNRKVc1sxvr+ETh8xO/XHsWb7JmcDXa1WI47yvjOI2rufaoaIpAqED1KziwmoJc29XBvYHb+xw2aeJyhfEsAO80/T"
            + "No7EZ6jpCUYPix7dhGOOgTG1P60+6yjMHSXpCBUaQw3C7Syn96SvncFEoIuPVD4LsFn8RVbo2kFbnOT4avESggm9cJaQSrE/BPMq"
            + "9Rm9k+pMHIYKNdc/kRaSqh95R/j1SXux6ERawWWi28SetR9k9sK3kV3gTz24P/QwEoaCZyTRS5Io38Ph1vJ4TnRrLy/K3ZPZ9e0G"
            + "/aipTalMBaJhd/jgBRy6zatiSbwEKSOPS1CsIanzS23LvzCe9DofXBJIMa7LxoXKOOGnxZSdSK78RuDsH9XQXznc6LJ2/F0Z/5wm"
            + "Di1xwSIdzNQ3HK+UYOSR8PZtFF96HGhQHgjllULwPo14Au5MO8stDpqwIR6TrLSp1atu5OO2NuEscriP0OM9uGu87aakQUv7FVMX"
            + "o3UC9hf3VqZahUy1KQ3KoCYfBAysUUzWSGO4InhKTqLyFTIeVBT8Y61eycKOBP9Huzo3KsAwEs8iDBq95Z89ekkTrAzrsY4aPQrU"
            + "e6qnPvoVYOLBI8BTS2oT9w3irmMIpC9T534OADZ7Tt45vziZtz9wugGYpUnq7w6lvYiFvnRiZqaRYKL4Vy+1JLIXFg2MBppsJOFZ"
            + "4oW6vKLlwsNFoqifmICcpSJ/CkYa/fusl7N4GlTiMZHMGYAn+v133DrKWN99G+iI7yp+Xm9KTHslE5/9c1e3ENB9EtSnhr34+94l"
            + "AYJCAJnrdyax/e6MaxZL/w9f/bd1fIyVnrqGprcaxxQc5QK+qx+aDHnWBDxZf3KDatg1KEXxSpoE9MK8r8BqlV5uo+egwuUC/1Yr"
            + "6HPiLdj/4EF4nzChShbgmxShSSpc8oeBR/xcyF+v8lgh9AiyjYA23KgxTLKzHgkeS1+2hK6olhnd1Nn1r7iQq9ExbnEdXX4u/xv7"
            + "pczmhdp6Ci3QSTypoBB/nZWFLQgq3CC5n2eFiB5YJp4ov7P9T6UjUoJWmQSCOCVcuWY84r9iYEkWq5vYx971BUxdbUxGux6JCx/t"
            + "SguxTeoOAoZ83ifR6vvTFkyFBuwUOQ/AZ1B0fLsMKoxy0Tt6kSdBjHrLxIMYhswVzRNNvEAj9qIlHGu/WTYQk/hvjdNtAozqv8wS"
            + "sd0E8f0nCYe8x7E+VfInhCf75YyMWogG+uN0Y6zFWXDc6cGDHw6M2ifuusFOSqJW7dY15TRJB/EQmoD20SraTl7/HDtc3q+CYig6"
            + "tzhV3jpjSNA6W/+NpHbbm5NkUOvzkilxeIvxDfDClMO3iYQpCtuJfv4jm3Q/A/iNxxEFms3Qgff+1QMaRn5ciJFj7TJxgSKn4hm7"
            + "yC/cBtklLWoPWnIEQoW/xG1kR8IkMOUHcu4VP6wmWrYHr0T5hPIpUOB1pG2lKAOu7QoYCxKaeA+XlqrqvIkh1/AMvTEzSFtef2vd"
            + "gZYFS0MBeJhW2clYR5b6hDshxg9g+GJZwZlJ0rtZ71deNqQD/m5SFhOe7cqoNt46RY1zdnrGeBx54TYsji6OcoK6mbWshgmY65Bw"
            + "bdwhQAFSh535MvefFLW4TxizrhqZaGeKAehK9EtR/MOhtu+cylSVig4zFFY3sCbM3UTpUEqokMMDnT7rgeUjQX0fqNvZC+y+oR3r"
            + "S/VugyDS6wOtFkxWhxHh+GQtBmpMImgQp9pDOiVfw6gnnbXx/Ced3p+15tRFTjGCI9CUp4J5DsQ9rjtq7YFQlk5w8ZEdfRfWdqHw"
            + "BcHO67O1mlUQm/u79d49Ga5TqJ4YEvWyhXYBSFZ3TujkyuzMbIC7pzf1F6cBU078Tf365l5k9PmY4Rafe51Pcg+ue2TXNVnoEa6x"
            + "OkTUdJqxKx8bQe2acb4jn7RXEtipPiiI728F6fBZSMMrksXLsXSx1GzybEm+kPqguMgBL3H6pqk6t+nv7HvD8ZNcHsW5sHS+b9Mj"
            + "10TzlVrW7D5llzfOh759NlYvHsj1uQpw9lIiMIkOtgae3AbeFtnuOO6WggglyKsdxAOUGAhN+uKFuyBDz/fmlRa5YvrRLCXjLReC"
            + "o/Cllo6bmypFCLFPTwS7ZN9JQ+a2j7lmJkLVB11uNzqs8Yh03r00ImNSBSfqeXP5M5c/ZBIxhFkk0G44svPVX+79LdR6bmRzHdxV"
            + "p46O9V5T2g/+FNJnKimhJqrPiOzRSqnYLpiBv9ODdAJavao3hYVznmABa9MGIlPr7MdzK1tR3PYvwbobQQgNfUTks7+DSm/+jHND"
            + "lOdoZ+LPK0lDrM7TftilzPQINqMURvpKwR6fm7n6t6s5HGxeFWKTOOlhl6DPqUPjm3D6LIrFkniDDk48U2SCrrdx3v+LAgfVtPhA"
            + "iGrorczB1TSaEklLWvB8QtldMJDc5shc4qBSSGny4+mPjzFO83KwAevsO1eOmKV4RwjgUszqRz7BM5msOmLwuboFcyMZEHIsUf/R"
            + "tuDCF5zG7QZ6eO1MzWe9ZrEumAN8yZe/ec96ws2kY1f3vw0+VirfE7ZK6HqblWZ/ueAUBnBmsQT4oN5vpLIZCV+uvdZxDUg9eA7v"
            + "zIHgrzKcKjJWLtiK8WogjLJZJLsmqH5QdzDIzwa4eiaijlNh3rNJrRxovTKwHJi9NT7o2YaeGgmIWy5HacvMzacs+9tMv5gL2TDI"
            + "CKeDRz8wI6BKoW3dbJ3uY/lScgMFlkhnewz2X+/LAXeT6B8HExzL9yzxYtJUpH6e+uQs8Qqq9VqzXnreBftaOOJ7zfU2p9Ld9HZd"
            + "Zs/9tkipFgmEsuJKK3vD4tHaAbtiBmp4xkJT52fcDuC9uDeyU07RaYZ0EpeaTSgZsGkmIPsnUzZ8OhYu4FALEiJlEKz0VPSFrIGp"
            + "KOxbl5F1L/N6PnqLfO37O6byA9TSXCbuDhkxioOlmLOQfHM4dqLbb7WyQMC/0UCwHNcAqZleJl/ptxP/cx3FRJIQ9dEOgIdFkp1y"
            + "2P5AdnSgaPJLOUuQxvLa7+NUL8uaIVoGX5dPQ+MXQR2K92fsOTxMH2ETmbVgC7SJ9Hxj+9v8YcRYy9ppO5mzFNxWpGdBZSYpW4J+"
            + "dU6mwF0nT7Fr6vsVR8Ac9ID2DPHss0EHE5moXr4vZPtYOJHiTV/L7TmeRW04Awtev5ymOOdnOLrdoJztF3Nro/W19WPyxAcN4G7l"
            + "uY/er/YsxW2oVOKRZg6qm4F80gCoywHgZscuTArB/14Kiuc/Y+9zNimNnVadQz0wSvFJQW7/f55h5cxZN7ovVipKJ/DShUnpXtBw"
            + "ocoeoWS6WXs3h6kFc+MeCXdhMji/4TvhnL1YnGvePp7e1Tiibk+R/AtIlcINgU5z1CHetwcHh8ymnImKWRWB9tCMEHRxcaxzcb8l"
            + "/VQaaH3T79oicTxy9E5l0pk4XFaJB541EmTklH+MZFMjICnmllxTYqOgHf++sD6wY3fOt0tYi8RKQBDqYq8ok2ofuB65Axv8Meo+"
            + "5LC/G7Tm5XlRaWd0aPyL/6Wdou2l4cZtKKrqYCVzqcSU0mgumbljsck1anEaDyyJ9XsiySIdKxL127xZRdgYVcUGbhnXnM8ZzTqm"
            + "96w0B+onpJdv6a1RJLaS/V7kEb19CEVN6rVG5L+25cpEXizLEKb3eoEPRcAKiegN7fxvSrYtOOCYdbrt4TepA3JVv7t9GiQAF7fd"
            + "QkIEcg11TLALZw/JGi6A4nvs98UQJoPUO6BN/GaedgEbFI1HvdSHx5v4sITFeYeQ47OrqEn6S0msgl3s4dayjJ4OyMF8/e1Qdwmn"
            + "MUEMbx/v9dCYChaX67BDdUgIsQNWECRmBDmBGzS/dmIcIZv3ZM6qZpgK6lFekspq9CVTzTM2gZK/0t/aFPo/gi1zO0Qw6SEs31ld"
            + "I5U2hkwhbmsANFjRxs7ZxUPZHRIuCEs25NxMNnULJXptYIRbxhtnIGEUEOl/l9LkRtKu2ccbcr5u3Kd4HNkfRBQTTT9Ow3z12Jng"
            + "zks6jaXua22+C8QDpMXnkA9MDq0sBes5qxkq8/+gxgADP7PKowEeuGt/BXE0vSp4DXETxViYtjQuFXMgCadJqeJGh3wLm5vXI+xh"
            + "v1WbAVTsRPDx0QfD11VXwXXwT0ejjjOulM/OWsN2AuPqfn3bt8ykVXqlnhi40BLqnFbkAFz3XPgk2XHbNoiob6fQ8uxmN917ipvd"
            + "GMdWKvO7qhbwninL7UsR4OxlMHB7Zt/CgL7LjtX5efiwQyXTG5RsIt95ihVVWUZbgA1qACOaBorA8h1PEAHOR9GCwgOtnFKMjT1z"
            + "gztzyPm4+IArm/533ggwSr80f0Pwj3D1Kh4OQtxODGc+62z8xUv4JDJaxaGKa7ipkTXWE1L+aVYgB+DjO194vwya5Rwcq4dlHkRx"
            + "CQ9tV10Z4sotkhMLcnMReTdZwiWtUIrNmlVOB0VL2qrdYgYiDvZxCB2BPVQBEipu4z8O/k2Unil4Rfe780AbGXeejLcVQ25IwJt9"
            + "fd/c6/J+DhJNLtjScdggrbGAoLK+GzxdPwd/vyDf87mTc2mZLEPm3TOdT/maq6wzIoeETixkaW4/pyPMkXJuWIv4/RRLv3yLm52y"
            + "aQhDRpwwk8Eb+beUd2+9ZdwYxnRSsgSJp2lBar5IoH94WAoyNAac6fK7nz5zYnCN5y7g3QuWAzaI4skh3MZnds/+PN7b9zPa1Ud/"
            + "hxVjINegXFk+ZT8pHUGQUM5Zqwgg8DJdr61dBb4KbYFEkVdIlpmZH+pnZ7SmWUFsfm2YkIgl/B8rxdsDqZw3HeB/3QTe8Q9MQGA1"
            + "tbW/tb3bz60oYdDKJM0ZiylPZzAoYtWEdT2enqSdKXNCTrhfbHKFazfouprgPQzK2FMXq22w5EUyAea2umsVanV1q321DEGp0S5o"
            + "99Q+BhcCZYHjMLXKJdL0rf9vn+hNGncsC5gtRwZdGAW8ieySgbeLZ21V621FzD0V72/wLf+yt5Vie9KYRY/OppgD8Jc4EPJLt5Uv"
            + "+Y79l21X4VHsMCtmMyGJDbw0GBda4ycVj7MmUgSZ94bgigIw+lde5P3d2/e8Q3dm89k3787pzh0YNSttjcIk1sQXeXQPls8i1q1l"
            + "6aubWSsi6AOFG5JJADJmTYEYGChCZVOJRiOf0fR6RmoHty/UxK6d2oXXZ8R9Nfju/fFbo+YRO3JlGx70tVkQJUJXNRUpUvFp5dld"
            + "lEfNyuMs4SU0RsLPcOe5TelcjNWbuRQN9PWJDLL9sJHxFPHnfczLkTUUwUX6V7Sl93C3JC/scFL+Kegrw9mb0MdO2KSD1sc+BtSK"
            + "x/dnnIGAglQEcXqa1JFZP3EOq2RkmEDYfI8xVcel7O6qnW6YuWA2Un7Dbs6QajXhR1h4fXAaEeleJeTCoiqdB6awb3PcwAzYMCeE"
            + "Q3FAo72ae4UreUPGTDsP/BjLQy0NqHAYx6bwr9JPcpC79n0c/6C5cLpCYTEuHkWkZ/+ctoeSQD0nd8t/MLQCXlfOzNtikaSXr5DI"
            + "5eO82L8+CzbNjvCjjUo5O/ZqBladi9JHe9R6YX3LDMM0rofx/IAJULCYEQPnCanaA3sb89xPRfpsmrKkDcrPSncuBJuY/4ETmCAc"
            + "mtNPquf8wwXhVOts+PMLM+3VlhSm3N3YjC9VSf0EdxlMP16HgYlV+Sde+1YigmoUjqzgSrzRcXlSS2Al6VmIOQmVyzqUJ4ZS9sBA"
            + "6VY3nNv4EHHD34dZriD7Dc7FSKbPmtfVwhbUkvUsKP5aDJdxDjD3wuoB2XcBKe/fwDTZf8Bnd9pQWBkKwTx3DnCaQe+ZxbeouwLI"
            + "U+qtf3ZhV46y9EzHq/n2MN5yY+JUHnohxk4WmpSaCPZMpCCmxguTz0u2hRIyI20UCBaZ2pLFYk3my+SwJE/HCfb8Gd2gMG7brwsN"
            + "IAiovKOSH0L1Cao2xDF5wzOuR2DRv3CyNruwdYbOKLNnZSh8f5zJqUcqxVRPd1Wp5SxCHXR9Dj7khrwwN3vXtPENzdFjflDihXIH"
            + "ei4rgrf/jC+twJ3KVpeyxxI6s7USboGYBdDt9yUejYLLB2FY1NUaU/9qKWbSZWrSMNiQ1O63x8Ofq+FtQUsBlp9tnuoyxVpWW6qJ"
            + "HoKFN5yhFeQ6I23LJt0BqnqS3KMNqzcaqZBPSv4QsOTymt9MjIQr28qFkg3A7BEDYMImvqSVK52IDlEVrNZ1bEo+1s+XdiUPifam"
            + "pEkysZuxbJHHOg5XPSyB4sy2RE96m6zsjDt90bzg8b+dTzmr7I7iqb3Q3/XklaJSDIpC1Tihki6zgZv/umfePzfPygIGZLzgzvmx"
            + "zisvRI9G84k1d9u/JJMjCGn8mnMCA6Gb1hRqn0qo19I37k1a6PI=");

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
