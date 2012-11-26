package org.eknet.publet.vfs

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 07.05.12 12:35
 *
 */
class PathSuite extends FunSuite with ShouldMatchers {

  test ("rebase simple paths") {
    val p0 = Path("/a/b/c/d.html")
    val p1 = Path("/a/b/x/y/z.html")
    val reb = p0.rebase(p1)
    reb.asString should equal ("../../c/d.html")
  }

  test ("parse simple paths") {
    var simple = "/a/b/c.html"
    var p = Path(simple)

    p.segments should have size (3)
    p.query should be (None)
    p.asString should be (simple)

    simple = "/ab/c/d.json?id=15&maxw=500"
    p = Path(simple)
    p.segments should have size (3)
    p.query should be (Some("id=15&maxw=500"))
  }

  test ("parse evil paths") {
    val evilPath =
      """/node/add/blog?format=1&field_offsite_link[0][format]=1&taxonomy[3][]=53&taxonomy[tags][2]=Trouble-Free Solutions In payday loans online - A Closer Look&body=What to Look for When Shopping for an Online Payday Loan Service. UK payday loans online could possibly be your better choice if you'd like instant cash at this time. Banks can take literally weeks over a decision and if you need a new exhaust on your car you need it now, not three weeks down the line, so instant money is the answer. Look to inquire about different lending establishments will assist to guarantee that you will come across a reputable firm to do financial dealings with. Even though a payday loan's purpose is obtaining fast money, you should not rush signing with a lender.
        |
        |  *How do I get a payday loan. When to Use a Payday Loan. Take affixed safeguards before submitting your intimate information online. This can speak volumes. There are also clauses in many lending contracts that do not allow the borrower to bring a lawsuit against a lender for any reason.
        |
        |  t worry if you need finance right away. You never know when the financial crisis may arise in your life. - Most lenders will be able to get you your money within 24 hours on regular business days. it is perhaps a worry that people might get themselves in too deep and spiral into borrowing to pay off other borrowing. Online lenders vary in terms of willingness to lend based on your profile.
        |
        |  If you get paid once a month, then you will have till then to come up with a payment. This is a way for lending companies to reach out to a wide variety of consumers. You can get a loan of. 12 Months Payday Loans  are the investments that are accessible to all kind of character. Once you paid for the repair, the time may come that you need to pay other bills and loans, but it's not payday yet.
        |
        |  This extra payment is much larger than other creditors because of the higher term interest rate. Those who are applying for these loans have complete freedom to spend the acquired as per their own wish and need. Whether they're online payday loans or borrowed through one of the neighborhood outlets of a lending company, the money coming from these loans is a considerable help during times that you need money right away. Nowadays, we can find a lot of websites that claim that they are legitimate payday loans company. When the loan is extended, the interest will accrue for each following terms.
        |  <a href="http://paydayloansonline.paydayloan-online.org/">payday loans online</a>
        |  http://paydayloansonline.paydayloan-online.org/&title=Trouble-Free Solutions In payday loans online - A Closer Look&field_offsite_link[0][value]=http://paydayloansonline.paydayloan-online.org/&op=&Approve Actions and Articles=2""".stripMargin

    val p = Path(evilPath)
    p.segments should have size (3)
    p.query.get.length should be (evilPath.substring("/node/add/blog?".length).length)
    p.query.get.contains("""<a href="http://paydayloansonline.paydayloan-online.org/">payday loans online</a>""") should be (true)
  }
}
