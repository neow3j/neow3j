# neow3j-crypto

This README aims to document some of the crypto mechanisms used by the NEO
blockchain and standards.

## Curve

NEO uses `secp256r1` as the elliptic curve for encryption.

More info about `secp256r1` can be found here:

* http://www.secg.org/SEC2-Ver-1.0.pdf
* https://www.ietf.org/rfc/rfc5480.txt
* http://www.hyperelliptic.org/tanja/vortraege/20130531.pdf
* https://crypto.stackexchange.com/questions/18965/is-secp256r1-more-secure-than-secp256k1

## Address Format

Addresses on the NEO network are built based on the private-public key concept.
For example, a valid NEO address is `AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ`.

First of all, each address is associated to a scripthash (20 byte array).
For example, the address `AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ` has the
scripthash `295f83f83fc439f56e6e1fb062d89c6f538263d7` (hex). In order to
build the scripthash, we need to get aware about [NEO opcodes](https://github.com/neo-project/neo-vm/blob/master/src/neo-vm/OpCode.cs)
since we should build a kind of script that executes instructions, and
then hash it. Literally speaking, a scripthash is a "hashed set of instructions".

The questions you might ask are:
* How such scripthash is built?
* How to transform an scripthash to an actual NEO address?

To answer these questions, follow the steps:

1. Generate a public-private key pair.
2. Take the encoded public key of the public-private key pair. For example, the encoded public key for the address `AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ`
is `0265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6`, while the unencoded one is: `0465bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d601d2ea55bbc8eb03bc449a2a1692c2521714ef31c7183ea098f27b7098e8981c`.
3. For a basic NEO address (i.e., not multi-signature), the scripthash should be formatted as follows (hex format):

```
21 0265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6 ac
```

where:

 * `21` represents the `PUSHBYTES33` opcode, that 33 bytes will be pushed in the next parameter;
 * `0265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6` is the encoded public key (33 bytes);
 * and `ac` represents the `CHECKSIG` opcode, to verify that the witness who signed the transaction should use the correct corresponding private key.

4. Take the `210265bf906bf385fbf3f777832e55a87991bcfbe19b097fb7c5ca2e4025a4d5e5d6ac` raw script
and first hash using [RIPEMD-160](https://en.wikipedia.org/wiki/RIPEMD#RIPEMD-160_hashes). Take the result of the RIPEMD-160 hash and
then hash using [SHA-256](https://en.wikipedia.org/wiki/SHA-2). The result is the scripthash `295f83f83fc439f56e6e1fb062d89c6f538263d7`.

5. To transform an script hash to a NEO address, it's required to perform such steps:

   5.1. First take the hex `17` representing the coin version and concatenate
   with the scripthash `295f83f83fc439f56e6e1fb062d89c6f538263d7`,
   resulting in `17295f83f83fc439f56e6e1fb062d89c6f538263d7` (hex format).

   5.2. Then, it's required to compute the checksum of such concatenation by hashing it
   **twice** with [SHA-256](https://en.wikipedia.org/wiki/SHA-2). In our example, the output
   is `ce9e45ea76472571d66d002a3726104015e656df78491cc6e9d3df4bf71bc13c`. The checksum is the
   first 4 bytes of the double SHA-256 output, which is `ce9e45ea`.

   5.3. Finally, concatenate both parts resulting in the byte array
   `17295f83f83fc439f56e6e1fb062d89c6f538263d7ce9e45ea`.

   5.4. Compute the [Base58](https://en.wikipedia.org/wiki/Base58)
   of `17295f83f83fc439f56e6e1fb062d89c6f538263d7ce9e45ea`
   to get the NEO address `AKYdmtzCD6DtGx16KHzSTKY8ji29sMTbEZ`.

## Wallet and Account

The wallet and accounts are implemented following the
[NEP-6](https://github.com/neo-project/proposals/blob/master/nep-6.mediawiki)
and [NEP-2](https://github.com/neo-project/proposals/blob/master/nep-2.mediawiki),
respectively.

## Wallet Import Format (WIF)

Same as the Bitcoin WIF format. Refer to the [Bitcoin documentation](https://en.bitcoin.it/wiki/Wallet_import_format).