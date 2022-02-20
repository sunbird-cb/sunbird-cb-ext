package org.sunbird.progress.cassandrarepo;

import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MandatoryContentRepository
		extends CassandraRepository<MandatoryContentModel, MandatoryContentPrimaryKeyModel> {
	/**
	 * Fetch the mandatory content for Org
	 *
	 * @param rootOrg
	 * @param org
	 * @return
	 */
	@Query("select content_id, content_type, minprogressforcompletion, batch_id from mandatory_user_content where root_org=?0 and org=?1")
	public List<MandatoryContentModel> getMandatoryContentsInfo(String rootOrg, String org);
}